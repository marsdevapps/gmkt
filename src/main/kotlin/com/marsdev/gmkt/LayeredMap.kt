package com.marsdev.gmkt

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.layout.Region

class LayeredMap(val baseMapProvider: BaseMapProvider) : Region() {
    internal val MAXZOOM = 16
    private var mapArea: BaseMap
    private var x0: Double = 0.toDouble()
    private var y0: Double = 0.toDouble()
    private val layers = FXCollections.observableArrayList<MapLayer>()
    private val provider = SimpleObjectProperty<BaseMapProvider>()

    init {
        this.provider.set(baseMapProvider)
        this.mapArea = baseMapProvider.getBaseMap()
        this.mapArea!!.install()

        this.children.add(mapArea!!.getView())
        this.layers.addListener(ListChangeListener<MapLayer> { c ->
            while (c.next()) {
                for (candidate in c.addedSubList) {
                    val view = candidate.getView()
                    children.add(view)
                    candidate.gotLayeredMap(this@LayeredMap)
                }
                for (target in c.removed) {
                    children.remove(target.getView())
                }
            }
        })
        setOnMousePressed { t ->
            x0 = t.sceneX
            y0 = t.sceneY
        }
        setOnMouseDragged { t ->
            mapArea!!.moveX(x0 - t.sceneX)
            mapArea!!.moveY(y0 - t.sceneY)
            x0 = t.sceneX
            y0 = t.sceneY
        }
        setOnZoom { t -> mapArea!!.zoom(if (t.zoomFactor > 1) .1 else -.1, (x0 + t.sceneX) / 2.0, (y0 + t.sceneY) / 2.0) }
        val zoomGestureEnabled = java.lang.Boolean.valueOf(System.getProperty("com.sun.javafx.gestures.zoom", "false"))!!
        if (!zoomGestureEnabled) {
            setOnScroll { t -> mapArea!!.zoom(if (t.deltaY > 1) .1 else -.1, t.sceneX, t.sceneY) }
        }
    }

    fun setBaseMapProvider(provider: BaseMapProvider) {
        this.provider.set(provider)
        resetBaseMap()
    }

    private fun resetBaseMap() {

        val zm = zoomProperty().get()
        val lat = centerLatitudeProperty().get()
        val lng = centerLongitudeProperty().get()

        mapArea.uninstall()
        this.children.remove(mapArea.getView())
        this.mapArea = provider.get().getBaseMap()
        this.children.add(0, mapArea.getView())
        this.mapArea.install()

        this.mapArea.setZoom(zm)
        this.mapArea.setCenter(lat, lng)

        synchronized(layers) {
            for (ml in layers) {
                ml.gotLayeredMap(this)
            }
        }

        //        this.mapArea.minHeightProperty().bind(map.heightProperty());
        //		this.mapArea.minWidthProperty().bind(map.widthProperty());

    }

    /**
     * Explicitly set the zoom level for this map. The map will be zoomed
     * with the center of the map as pivot
     * @param z the zoom level
     */
    fun setZoom(z: Double) {
        val s = this.scene
        val x = if (s == null) 0 else s.width / 2
        val y = if (s == null) 0 else s.width / 2
        setZoom(z, x as Double, y as Double)
    }

    /**
     * Explicitly set the zoom level for this map. The map will be zoomed
     * around the supplied x-y coordinates
     * @param z the zoom level
     * *
     * @param x the pivot point, in pixels from the origin of the map
     * *
     * @param y the pivot point, in pixels from the origin of the map
     */
    fun setZoom(z: Double, x: Double, y: Double) {
        val delta = z - this.mapArea!!.zoomProperty().get()
        this.mapArea!!.zoom(delta, x, y)
    }

    /**
     * Explicitly center the map around this location
     * @param lat latitude
     * *
     * @param lon longitude
     */
    fun setCenter(lat: Double, lon: Double) {
        this.mapArea!!.setCenter(lat, lon)
    }

    /**
     * Explicitly show the rectangular viewport on the map. The center of the viewport will
     * be the center of the map. The map is scaled as big as possible,
     * ensuring though that the viewport is visible.
     * Viewport is provided by the north-east and south-west corners.
     * @param lat1 latitude north-east
     * *
     * @param lon1 longitude north-east
     * *
     * @param lat2 latitude south-west
     * *
     * @param lon2 longitude south-west
     */
    fun setViewport(lat1: Double, lon1: Double, lat2: Double, lon2: Double) {
        val latdiff = lat1 - lat2
        val londiff = lon1 - lon2
        val log2 = Math.log(2.0)

        var localWidth = this.scene.width
        var localHeight = this.scene.height
        if (this.parent != null) {
            val bounds = this.parent.boundsInParent
            localWidth = bounds.width
            localHeight = bounds.height
        }


        // Scene scene = this.mapArea.getView().getScene();
        val tileX = localWidth / 256
        val tileY = localHeight / 256
        val latzoom = Math.log(180 * tileY / latdiff) / log2
        val lonzoom = Math.log(360 * tileX / londiff) / log2
        val centerX = lat2 + latdiff / 2
        val centerY = lon2 + londiff / 2
        val z = Math.min(MAXZOOM.toDouble(), Math.min(latzoom, lonzoom))
        this.mapArea!!.setZoom(z)
        this.mapArea!!.setCenter(centerX, centerY)
    }

    /**
     * Return the MapArea that is backing this map
     * @return the MapArea used as the geomap for this layeredmap
     */
    fun getMapArea(): BaseMap {
        return this.mapArea as BaseMap
    }

    /**
     * Return a mutable list of all layers that are handled by this LayeredMap
     * The MapArea backing the map is not part of this list
     * @return the list containing all layers
     */
    fun getLayers(): ObservableList<MapLayer> {
        return layers
    }

    /**
     * Return the (x,y) coordinates for the provides (lat, lon) point as it
     * would appear on the current map, talking into account the zoom and
     * translate properties
     * @param lat
     * @param lon
     * @return
     */
    fun getMapPoint(lat: Double, lon: Double): Point2D {
        return this.mapArea!!.getMapPoint(lat, lon)
    }

    /**
     * Return the geolocation (lat/lon) for a given point on the screen
     * @param sceneX
     * @param sceneY
     * @return
     */
    fun getMapPosition(sceneX: Double, sceneY: Double): Position {
        return this.mapArea!!.getMapPosition(sceneX, sceneY)
    }

    /**
     * Return the zoom property for the backing map
     * @return the zoom property for the backing map
     */
    fun zoomProperty(): DoubleProperty {
        return this.mapArea!!.zoomProperty()
    }

    /**
     * Return the horizontal translation of the backing map
     * @return the horizontal translation of the backing map
     */
    fun xShiftProperty(): DoubleProperty {
        return this.mapArea!!.getView().translateXProperty()
    }

    /**
     * Return the vertical translation of the backing map
     * @return the vertical translation of the backing map
     */
    fun yShiftProperty(): DoubleProperty {
        return this.mapArea!!.getView().translateYProperty()
    }

    fun centerLongitudeProperty(): DoubleProperty {
        return this.mapArea!!.centerLongitude()
    }

    fun centerLatitudeProperty(): DoubleProperty {
        return this.mapArea!!.centerLatitude()
    }

    fun addNode(n: Node) {
        this.children.add(n)
    }

}
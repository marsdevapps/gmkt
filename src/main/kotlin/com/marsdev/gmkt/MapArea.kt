package com.marsdev.gmkt

import javafx.beans.InvalidationListener
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.shape.Rectangle
import java.lang.Math.ceil
import java.lang.Math.floor
import java.lang.ref.SoftReference
import java.util.*

/**

 * @author johan
 */
class MapArea(val incomingTileType: ObjectProperty<MapTileType>) : Group(), BaseMap {

    private var nearestZoom: Int = 0
    private val tiles = arrayOfNulls<HashMap<*, *>>(MAX_ZOOM)
    private val zoomProperty = SimpleDoubleProperty()

    private var lat: Double = 0.toDouble()
    private var lon: Double = 0.toDouble()
    private var abortedTileLoad: Boolean = false

    private val debug = false
    private val area: Rectangle
    private val centerLon = SimpleDoubleProperty()
    private val centerLat = SimpleDoubleProperty()

    private var sceneListener: InvalidationListener? = null
    private val tileType = SimpleObjectProperty<MapTileType>();

    init {
        this.tileType.bind(incomingTileType)

        for (i in tiles.indices) {
            tiles[i] = HashMap<Long, SoftReference<MapTile>>()
        }
        area = Rectangle(-10.0, -10.0, 810.0, 610.0)
        area.isVisible = false

        //        area.translateXProperty().bind(translateXProperty().multiply(-1));
        //        area.translateYProperty().bind(translateYProperty().multiply(-1));
        //        this.sceneProperty().addListener(i -> {
        //            if (getScene() != null) {
        //                area.widthProperty().bind(getScene().widthProperty().add(20));
        //                area.heightProperty().bind(getScene().heightProperty().add(20));
        //                if (abortedTileLoad) {
        //                    abortedTileLoad = false;
        //                    setCenter(lat, lon);
        //                }
        //            }
        //        });
        zoomProperty.addListener { ov, t, t1 -> nearestZoom = Math.min(floor(t1.toDouble() + TIPPING).toInt(), MAX_ZOOM - 1) }

        this.tileType.addListener({ obs: ObservableValue<out MapTileType>, o: MapTileType, n: MapTileType ->
            println("TileType was changed: " + n!!)
            if (n != null) {
                reloadTiles()
            } else {
                clearTiles()
            }
        })

    }

    fun tileTypeProperty(): ObjectProperty<MapTileType> {
        return tileType
    }

    override fun setCenter(lat: Double, lon: Double) {
        this.lat = lat
        this.lon = lon
        if (scene == null) {
            abortedTileLoad = true
            if (debug) {
                println("Ignore setting center since scene is null.")
            }
            return
        }
        var localWidth = this.scene.width
        var localHeight = this.scene.height
        if (this.parent != null && this.parent.parent != null) {
            val bounds = this.parent.parent.boundsInParent
            localWidth = bounds.width
            localHeight = bounds.height
        }
        val activeZoom = zoomProperty.get()
        val n = Math.pow(2.0, activeZoom)
        val lat_rad = Math.PI * lat / 180
        val id = n / 360.0 * (180 + lon)
        val jd = n * (1 - Math.log(Math.tan(lat_rad) + 1 / Math.cos(lat_rad)) / Math.PI) / 2
        val mex = id.toDouble() * 256
        val mey = jd.toDouble() * 256
        val ttx = mex - localWidth / 2
        val tty = mey - localHeight / 2
        translateX = -1 * ttx
        translateY = -1 * tty
        if (debug) {
            println("setCenter, tx = " + this.translateX + ", with = " + this.scene.width / 2 + ", mex = " + mex)
        }
        loadTiles()
    }

    /**
     * Move the center of the map horizontally by a number of pixels. After this
     * operation, it will be checked if new tiles need to be downloaded

     * @param dx the number of pixels
     */
    override fun moveX(dx: Double) {
        translateX = translateX - dx
        loadTiles()
    }

    /**
     * Move the center of the map vertically by a number of pixels. After this
     * operation, it will be checked if new tiles need to be downloaded

     * @param dy the number of pixels
     */
    override fun moveY(dy: Double) {
        val zoom = zoomProperty.get()
        val maxty = 256 * Math.pow(2.0, zoom) - this.scene.height
        if (debug) {
            println("ty = $translateY and dy = $dy")
        }
        if (translateY <= 0) {
            if (translateY + maxty >= 0) {
                translateY = Math.min(0.0, translateY - dy)
            } else {
                translateY = -maxty + 1
            }
        } else {
            translateY = 0.0
        }
        loadTiles()
    }

    override fun setZoom(z: Double) {
        if (debug) {
            println("setZoom called")
        }
        zoomProperty.set(z)
        setCenter(this.lat, this.lon)
    }

    override fun zoom(delta: Double, pivotX: Double, pivotY: Double) {
        val dz = delta// > 0 ? .1 : -.1;
        val zp = zoomProperty.get()
        if (debug) {
            println("Zoom called, zp = $zp, delta = $delta, px = $pivotX, py = $pivotY")
        }
        val txold = translateX
        val t1x = pivotX - translateX
        val t2x = 1.0 - Math.pow(2.0, dz)
        val totX = t1x * t2x
        val tyold = translateY
        val t1y = pivotY - tyold
        val t2y = 1.0 - Math.pow(2.0, dz)
        val totY = t1y * t2y
        if (debug) {
            println("zp = $zp, txold = $txold, totx = $totX, tyold = $tyold, toty = $totY")
        }
        if (delta > 0) {
            if (zp < MAX_ZOOM) {
                translateX = txold + totX
                translateY = tyold + totY
                zoomProperty.set(zp + delta)
                loadTiles()
            }
        } else {
            if (zp > 1) {
                val nz = zp + delta
                if (Math.pow(2.0, nz) * 256 > this.scene.height) {
                    // also, we need to fit on the current screen
                    translateX = txold + totX
                    translateY = tyold + totY
                    zoomProperty.set(zp + delta)
                    loadTiles()
                } else {
                    println("sorry, would be too small")
                }
            }
        }
        if (debug) {
            println("after, zp = " + zoomProperty.get())
        }
    }

    override fun zoomProperty(): DoubleProperty {
        return zoomProperty
    }

    override fun getMapPoint(lat: Double, lon: Double): Point2D {
        return getMapPoint(zoomProperty.get(), lat, lon)
    }

    private fun getMapPoint(zoom: Double, lat: Double, lon: Double): Point2D {
        val n = Math.pow(2.0, zoom)
        val lat_rad = Math.PI * lat / 180
        val id = n / 360.0 * (180 + lon)
        val jd = n * (1 - Math.log(Math.tan(lat_rad) + 1 / Math.cos(lat_rad)) / Math.PI) / 2
        val mex = id.toDouble() * 256
        val mey = jd.toDouble() * 256
        val ttx = mex - this.scene.width / 2
        val tty = mey - this.scene.height / 2
        val x = this.translateX + mex
        val y = this.translateY + mey
        val answer = Point2D(x, y)
        return answer
    }

    override fun getMapPosition(sceneX: Double, sceneY: Double): Position {
        val x = sceneX - this.translateX
        val y = sceneY - this.translateY
        val zoom = zoomProperty().get()
        val latrad = Math.PI - 2.0 * Math.PI * y / (Math.pow(2.0, zoom) * 256.0)
        val mlat = Math.toDegrees(Math.atan(Math.sinh(latrad)))
        val mlon = x / (256 * Math.pow(2.0, zoom)) * 360 - 180
        return Position(mlat, mlon)
    }

    private fun calculateCenterCoords() {
        val x = this.scene.width / 2 - this.translateX
        val y = this.scene.height / 2 - this.translateY
        val zoom = zoomProperty.get()
        val latrad = Math.PI - 2.0 * Math.PI * y / (Math.pow(2.0, zoom) * 256.0)
        val mlat = Math.toDegrees(Math.atan(Math.sinh(latrad)))
        val mlon = x / (256 * Math.pow(2.0, zoom)) * 360 - 180
        centerLon.set(mlon)
        centerLat.set(mlat)
    }

    override fun centerLongitude(): DoubleProperty {
        return centerLon
    }

    override fun centerLatitude(): DoubleProperty {
        return centerLat
    }

    private fun loadTiles() {
        if (scene == null) {
            return
        }
        val activeZoom = zoomProperty.get()
        val deltaZ = nearestZoom - activeZoom
        val i_max = (1 shl nearestZoom).toLong()
        val j_max = (1 shl nearestZoom).toLong()
        val tx = translateX
        val ty = translateY
        val width = scene.width
        val height = scene.height
        val imin = Math.max(0, (-tx * Math.pow(2.0, deltaZ) / 256).toLong() - 1)
        val jmin = Math.max(0, (-ty * Math.pow(2.0, deltaZ) / 256).toLong())
        val imax = Math.min(i_max, imin + (width * Math.pow(2.0, deltaZ) / 256).toLong() + 3)
        val jmax = Math.min(j_max, jmin + (height * Math.pow(2.0, deltaZ) / 256).toLong() + 3)
        if (debug) {
            println("zoom = $nearestZoom, active = $activeZoom, tx = $tx, loadtiles, check i-range: $imin, $imax and j-range: $jmin, $jmax")
        }
        for (i in imin..imax - 1) {
            for (j in jmin..jmax - 1) {
                val key = i * i_max + j
                var ref: SoftReference<MapTile>
                if (tiles[nearestZoom]!![key] == null) {
                    var tile = MapTile(this, nearestZoom, i, j)
                    (tiles[nearestZoom] as HashMap<Long, SoftReference<MapTile>>).put(key, SoftReference(tile))
                    val covering = tile.getCoveringTile()
                    if (covering != null) {
                        if (!children.contains(covering)) {
                            children.add(covering)
                        }
                    }
                    children.add(tile)
                } else {
                    ref = tiles[nearestZoom]!![key] as SoftReference<MapTile>
                    val tile = ref.get()
                    if (!children.contains(tile)) {
                        children.add(tile)
                    }
                }
            }
        }
        calculateCenterCoords()
        cleanupTiles()

    }

    /**
     * Find the "nearest" lower-zoom tile that covers a specific tile. This is
     * used to find out what tile we have to show while a new tile is still
     * loading

     * @param zoom
     * *
     * @param i
     * *
     * @param j
     * *
     * @return the lower-zoom tile which covers the specified tile
     */
    fun findCovering(zoom: Int, i: Long, j: Long): MapTile? {
        var zoom = zoom
        var i = i
        var j = j
        while (zoom > 0) {
            zoom--
            i = i / 2
            j = j / 2
            val candidate = findTile(zoom, i, j)
            if (candidate != null && !candidate!!.loading()) {
                return candidate
            }
        }
        return null
    }

    /**
     * Return a specific tile

     * @param zoom the zoomlevel
     * *
     * @param i the x-index
     * *
     * @param j the y-index
     * *
     * @return the tile, only if it is still in the cache
     */
    private fun findTile(zoom: Int, i: Long, j: Long): MapTile? {
        val key = i * (1 shl zoom) + j
        if (tiles[zoom]!![key] != null) {
            val exists: SoftReference<MapTile> = tiles[zoom]!![key] as SoftReference<MapTile>
            return exists.get()
        } else {
            return null
        }
    }

    private fun cleanupTiles() {
        if (debug) {
            println("START CLEANUP")
        }
        val zp = zoomProperty.get()
        val toRemove = LinkedList<MapTile>()
        val parent = this.parent
        val children = this.children
        for (child in children) {
            if (child is MapTile) {
                val tile = child as MapTile
                val intersects = tile.getBoundsInParent().intersects(area.boundsInParent)
                if (debug) {
                    println("evaluate tile " + tile + ", is = " + intersects + ", tzoom = " + tile.getZoomLevel())
                }
                if (!intersects) {
                    if (debug) println("not shown")
                    val loading = tile.loading()
                    //    System.out.println("Reap "+tile+" loading? "+loading);
                    if (!loading) {
                        toRemove.add(tile)
                    }
                } else if (tile.getZoomLevel() > ceil(zp)) {
                    if (debug) println("too detailed")
                    toRemove.add(tile)
                } else if (tile.getZoomLevel() < floor(zp + TIPPING) && !tile.isCovering() && ceil(zp) < MAX_ZOOM) {
                    if (debug) println("not enough detailed")
                    toRemove.add(tile)
                }
            }
        }

        getChildren().removeAll(toRemove)

        if (debug) {
            println("DONE CLEANUP")
        }
    }


    /** Reload all tiles on a change in provider. There could be a more
     * efficient way?
     */
    private fun reloadTiles() {

        println("TileType was changed, reloading tiles.")

        clearTiles()

        loadTiles()

    }

    private fun clearTiles() {

        val toRemove = ArrayList<Node>()
        val children = this.children
        for (child in children) {
            if (child is MapTile) {
                toRemove.add(child)
            }
        }
        getChildren().removeAll(children)

        for (i in tiles.indices) {
            tiles[i]?.clear()
        }

    }

    override fun getView(): Node {
        return this
    }

    override fun install() {

        area.translateXProperty().bind(translateXProperty().multiply(-1))
        area.translateYProperty().bind(translateYProperty().multiply(-1))
        if (sceneListener == null) {
            sceneListener = InvalidationListener {
                if (scene != null) {
                    area.widthProperty().bind(scene.widthProperty().add(20))
                    area.heightProperty().bind(scene.heightProperty().add(20))
                }
                if (abortedTileLoad) {
                    abortedTileLoad = false
                    setCenter(lat, lon)
                }
            }
        }
        this.sceneProperty().addListener(sceneListener)

    }

    override fun uninstall() {
        this.sceneProperty().removeListener(sceneListener)
        area.translateXProperty().unbind()
        area.translateYProperty().unbind()
        area.widthProperty().unbind()
        area.heightProperty().unbind()
        clearTiles()
    }

    companion object {

        /**
         * When the zoom-factor is less than TIPPING below an integer, we will use
         * the higher-level zoom and scale down.
         */
        val TIPPING = 0.2

        /**
         * The maximum zoom level this map supports.
         */
        val MAX_ZOOM = 20
    }
}
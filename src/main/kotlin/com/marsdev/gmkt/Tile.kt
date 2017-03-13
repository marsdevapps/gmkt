package com.marsdev.gmkt

import javafx.beans.InvalidationListener
import javafx.beans.WeakInvalidationListener
import javafx.beans.property.SimpleBooleanProperty
import javafx.concurrent.Worker
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.transform.Scale
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class Tile : Region() {

    //static final String TILESERVER = "http://tile.openstreetmap.org/";//
    //static final String TILESERVER = "http://otile1.mqcdn.com/tiles/1.0.0/map/";
    private val mapArea: Area
    private val myZoom: Int
    private val i: Long
    private val j: Long
    private val covering = LinkedList<Tile>()

    private val debug = false

    private val debugLabel = Label()
    internal var createcnt = AtomicInteger(0)

    /**
     * In most cases, a tile will be shown scaled. The value for the scale
     * factor depends on the active zoom and the tile-specific myZoom
     */
    internal val scale = Scale()

    private var temporaryImage: Image
    static
    {
        WritableImage writableImage = new WritableImage(256, 256);
        for (int x = 0; x < 256; x++) {
        for (int y = 0; y < 256; y++) {
        writableImage.getPixelWriter().setColor(x, y, Color.rgb(128, 128, 128));
    }
    }
        temporaryImage = writableImage;
    }

    private var zl: InvalidationListener
    private var iwpl: InvalidationListener
    private val loading = SimpleBooleanProperty()
    private var parentTile: Tile?
    private var imageWorker: Worker<Image>

    /**
     * Create a specific MapTile for a zoomlevel, x-index and y-index

     * @param mapArea the mapArea that will hold this tile. We need a reference
     * * to the MapArea as it contains the active zoom property
     * *
     * @param zoom the zoom level for this tile
     * *
     * @param i the x-index (between 0 and 2^zoom)
     * *
     * @param j the y-index (between 0 and 2^zoom)
     */
    fun MapTile(mapArea: Area, zoom: Int, i: Long, j: Long)  {
        val ig = createcnt.incrementAndGet()
        if (debug) println("Create tile #" + ig)
        this.mapArea = mapArea
        this.myZoom = zoom
        this.i = i
        this.j = j
        scale.pivotX = 0.0
        scale.pivotY = 0.0
        getTransforms().add(scale)
        //String url = TILESERVER + zoom + "/" + i + "/" + j + ".png";
        //        InputStream is = mapArea.tileTypeProperty().get().getInputStream(zoom, i, j);// , ig, ig).getBaseURL() + zoom + "/" + i + "/" + j + ".png";
        //        if (debug) {
        //            System.out.println("Creating maptile " + this + " with is = " + is);
        //        }

        val iv = ImageView(temporaryImage)
        if (debug) debugLabel.text = "[$zoom-$i-$j]"
        getChildren().addAll(iv, debugLabel)

        imageWorker = mapArea.tileTypeProperty().get().getImage(zoom, i, j)
        loading.bind(imageWorker.progressProperty().lessThan(1.0))
        imageWorker.stateProperty().addListener { obs, ov, nv ->
            if (nv == Worker.State.SUCCEEDED) {
                iv.image = imageWorker.value
            }
        }

        parentTile = mapArea.findCovering(zoom, i, j)
        if (parentTile != null) {
            if (debug) println("[JVDBG] ASK " + parentTile + " to cover for " + this)

            parentTile!!.addCovering(this)
        }

        iwpl = createImageWorkerProgressListener()
        imageWorker.progressProperty().addListener(WeakInvalidationListener(iwpl))
        if (imageWorker.progress >= 1) {
            if (debug) println("[JVDBG] ASK " + parentTile + " to NOWFORGET for " + this)
            if (parentTile != null) {
                parentTile!!.removeCovering(this)
            }
        }
        zl = recalculate()

        mapArea.zoomProperty().addListener(WeakInvalidationListener(zl))
        mapArea.translateXProperty().addListener(WeakInvalidationListener(zl))
        mapArea.translateYProperty().addListener(WeakInvalidationListener(zl))
        calculatePosition()
    }

    /**
     * Return the zoomLevel of this tile. This can not be changed, it is a fixed
     * property of the tile.

     * @return the zoomLevel of this tile.
     */
    fun getZoomLevel(): Int {
        return myZoom
    }

    /**
     * Check if the image in this tile is still loading

     * @return true in case the image is still loading, false in case the image
     * * is loaded
     */
    fun loading(): Boolean {
        return loading.get()
    }

    /**
     * Indicate that we are used to cover the loading tile. As soon as we are
     * covering for at least 1 tile, we are visible.

     * @param me a (new) tile which image is still loading
     */
    fun addCovering(me: Tile) {
        covering.add(me)
        setVisible(true)
    }

    /**
     * Remove the supplied tile from the covering list, as its image has been
     * loaded.

     * @param me
     */
    fun removeCovering(me: Tile) {
        covering.remove(me)
        calculatePosition()
    }

    /**
     * Return the tile that will cover us while loading

     * @return the lower-level zoom tile that covers this tile.
     */
    fun getCoveringTile(): Tile {
        return parentTile
    }

    /**
     * Check if the current tile is covering more detailed tiles that are
     * currently being loaded.

     * @return
     */
    fun isCovering(): Boolean {
        return covering.size > 0
    }

    override fun toString(): String {
        return "Tile[$myZoom] $i, $j"
    }

    private fun recalculate(): InvalidationListener {
        return { o -> calculatePosition() }
    }

    private fun createImageWorkerProgressListener(): InvalidationListener {
        val answer = { o ->
            val progress = imageWorker.progress
            //            System.out.println("IPL, p = "+progress+" for "+this);
            if (progress >= 1.0) {
                if (parentTile != null) {
                    if (debug) println("[JVDBG] ASK " + parentTile + " to FORGET cover for " + this)

                    parentTile!!.removeCovering(this@Tile)
                }
            }
        }
        return answer
    }

    private fun calculatePosition() {
        val currentZoom = mapArea.zoomProperty().get()
        val visibleWindow = floor(currentZoom + MapArea.TIPPING).toInt()
        if (visibleWindow == myZoom || isCovering() || visibleWindow >= MapArea.MAX_ZOOM && myZoom == MapArea.MAX_ZOOM - 1) {
            this.setVisible(true)

        } else {
            this.setVisible(false)
        }
        if (debug) {
            println("visible tile " + this + "? " + this.isVisible() + if (this.isVisible()) " covering? " + isCovering() else "")
            if (this.isVisible() && this.isCovering()) {
                println("covering for " + this.covering)
            }
        }
        val sf = Math.pow(2.0, currentZoom - myZoom)
        scale.x = sf
        scale.y = sf
        setTranslateX(256.0 * i.toDouble() * sf)
        setTranslateY(256.0 * j.toDouble() * sf)
    }

}
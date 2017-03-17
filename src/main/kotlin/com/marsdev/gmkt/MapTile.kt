package com.marsdev.gmkt


import javafx.beans.InvalidationListener
import javafx.beans.Observable
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
import java.lang.Math.floor
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**

 * @author johan
 */
class MapTile(val mapArea: MapArea, val zoom: Int, val i: Long, val j: Long) : Region() {
    private val covering = LinkedList<MapTile>()

    private val debug = false

    private val debugLabel = Label()
    internal var createcnt = AtomicInteger(0)

    /**
     * In most cases, a tile will be shown scaled. The value for the scale
     * factor depends on the active zoom and the tile-specific myZoom
     */
    internal val scale = Scale()

    private var temporaryImage: Image
    private val zl: InvalidationListener
    private val iwpl: InvalidationListener
    private val loading = SimpleBooleanProperty()
    private val parentTile: MapTile?
    private val imageWorker: Worker<Image>

    init {
        val writableImage = WritableImage(256, 256)
        for (x in 0..255) {
            for (y in 0..255) {
                writableImage.pixelWriter.setColor(x, y, Color.rgb(128, 128, 128))
            }
        }
        temporaryImage = writableImage

        val ig = createcnt.incrementAndGet()
        if (debug) println("Create tile #" + ig)
        scale.pivotX = 0.0
        scale.pivotY = 0.0
        transforms.add(scale)
        //String url = TILESERVER + zoom + "/" + i + "/" + j + ".png";
//        InputStream is = mapArea.tileTypeProperty().get().getInputStream(zoom, i, j);// , ig, ig).getBaseURL() + zoom + "/" + i + "/" + j + ".png";
//        if (debug) {
//            System.out.println("Creating maptile " + this + " with is = " + is);
//        }

        val iv = ImageView(temporaryImage)
        if (debug) debugLabel.text = "[$zoom-$i-$j]"
        children.addAll(iv, debugLabel)

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

            parentTile.addCovering(this)
        }

        iwpl = createImageWorkerProgressListener()
        imageWorker.progressProperty().addListener(WeakInvalidationListener(iwpl))
        if (imageWorker.progress >= 1) {
            if (debug) println("[JVDBG] ASK " + parentTile + " to NOWFORGET for " + this)
            parentTile?.removeCovering(this)
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
        return zoom
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
    fun addCovering(me: MapTile) {
        covering.add(me)
        isVisible = true
    }

    /**
     * Remove the supplied tile from the covering list, as its image has been
     * loaded.

     * @param me
     */
    fun removeCovering(me: MapTile) {
        covering.remove(me)
        calculatePosition()
    }

    /**
     * Return the tile that will cover us while loading

     * @return the lower-level zoom tile that covers this tile.
     */
    fun getCoveringTile(): MapTile? {
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
        return "Tile[$zoom] $i, $j"
    }

    private fun recalculate(): InvalidationListener {
        return InvalidationListener { calculatePosition() }

    }

    private fun createImageWorkerProgressListener(): InvalidationListener {
        val answer = object : InvalidationListener {
            override fun invalidated(observable: Observable) {
                val progress = imageWorker.progress
                //            System.out.println("IPL, p = "+progress+" for "+this);
                if (progress >= 1.0) {
                    if (parentTile != null) {
                        if (debug) println("[JVDBG] ASK " + parentTile + " to FORGET cover for " + this)

                        parentTile.removeCovering(this@MapTile)
                    }
                }

            }
        }
        return answer
    }

    private fun calculatePosition() {
        val currentZoom = mapArea.zoomProperty().get()
        val visibleWindow = floor(currentZoom + MapArea.TIPPING).toInt()
        if (visibleWindow == zoom || isCovering() || visibleWindow >= MapArea.MAX_ZOOM && zoom == MapArea.MAX_ZOOM - 1) {
            this.isVisible = true

        } else {
            this.isVisible = false
        }
        if (debug) {
            println("visible tile " + this + "? " + this.isVisible + if (this.isVisible) " covering? " + isCovering() else "")
            if (this.isVisible && this.isCovering()) {
                println("covering for " + this.covering)
            }
        }
        val sf = Math.pow(2.0, currentZoom - zoom)
        scale.x = sf
        scale.y = sf
        translateX = 256.0 * i.toDouble() * sf
        translateY = 256.0 * j.toDouble() * sf
    }


}

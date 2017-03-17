package com.marsdev.gmkt.demo

import com.marsdev.gmkt.DefaultBaseMapProvider
import com.marsdev.gmkt.LayeredMap
import com.marsdev.gmkt.PositionLayer
import com.marsdev.gmkt.TileProvider
import com.marsdev.gmkt.providers.OSMTileProvider
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.shape.Rectangle
import javafx.stage.Stage

class MapViewDemo : Application() {
    lateinit internal var map: LayeredMap

    internal var tileProviders: Array<TileProvider>? = null

    override fun start(primaryStage: Stage?) {
        val provider = DefaultBaseMapProvider()
        val osmProvider = OSMTileProvider()
        provider.tileProviderProperty().set(osmProvider)
        provider.tileTypeProperty().set(osmProvider.getDefaultType())

        map = LayeredMap(provider)

        val cbp = BorderPane()
        cbp.center = map

        val clip = Rectangle(700.0, 600.0)
        cbp.clip = clip
        clip.heightProperty().bind(cbp.heightProperty())
        clip.widthProperty().bind(cbp.widthProperty())

        val bp = BorderPane()
        bp.center = cbp

        val scene = Scene(bp, 800.0, 650.0)
        primaryStage!!.setScene(scene)
        primaryStage.show()
        //   map.setZoom(4);
        map.setViewport(52.0, 4.9, 50.1, 4.0)
        //  map.setCenter(50.2, 4.2);
        showMyLocation()
        map.setZoom(10.0)

        val p = map.getMapArea().getMapPosition(10.0, 100.0)
        System.out.println("position = " + p.latitude + ", " + p.longitude)


    }

    private fun showMyLocation() {
        val im = this.javaClass.getResource("/com/marsev/gmkt/demo/mylocation.png")
        val image = Image(im.toString())
        val positionLayer = PositionLayer(ImageView(image), (image.width / -2.0), (image.height / -2.0))
        map.getLayers().add(positionLayer)
        positionLayer.updatePosition(51.2, 4.2)
        map.centerLatitudeProperty().addListener { i -> System.out.println("center of map: lat = " + map.centerLatitudeProperty().get() + ", lon = " + map.centerLongitudeProperty().get()) }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(MapViewDemo::class.java)
        }
    }
}

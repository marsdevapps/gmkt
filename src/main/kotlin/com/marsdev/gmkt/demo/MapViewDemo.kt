package com.marsdev.gmkt.demo

import com.marsdev.gmkt.*
import com.marsdev.gmkt.providers.MapBoxTileProvider
import com.marsdev.gmkt.providers.MapBoxTileType
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
        // set MapBox API Access Token and local directory to store cached tiles....
        val mbStreetsProvider = MapBoxTileProvider("", "")
        provider.tileProviderProperty().set(mbStreetsProvider)
        provider.tileTypeProperty().set(mbStreetsProvider.getTileType(MapBoxTileType.MAPBOX_STREETS))

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
        map.setViewport(30.0, -95.00, 29.1, -95.90)
//          map.setCenter(29.70, -95.81)
        showMyLocation()
        map.setZoom(10.0)

        val licenseLayer = LicenseLayer(provider)
        map.getLayers().add(licenseLayer)

        val p = map.getMapArea().getMapPosition(29.70, -95.81)
        System.out.println("position = " + p.latitude + ", " + p.longitude)


    }

    private fun showMyLocation() {
        val im = this.javaClass.getResource("/com/marsev/gmkt/demo/mylocation.png")
        val image = Image(im.toString())
        val positionLayer = PositionLayer(ImageView(image), (image.width / -2.0), (image.height / -2.0))
        map.getLayers().add(positionLayer)
        positionLayer.updatePosition(29.70, -95.81)
        map.centerLatitudeProperty().addListener { i -> System.out.println("center of map: lat = " + map.centerLatitudeProperty().get() + ", lon = " + map.centerLongitudeProperty().get()) }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(MapViewDemo::class.java)
        }
    }
}

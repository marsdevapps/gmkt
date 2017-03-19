package com.marsdev.gmkt

import com.marsdev.gmkt.providers.FileProvider
import com.marsdev.gmkt.providers.OSMTileProvider
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import java.util.*

class DefaultBaseMapProvider : BaseMapProvider {
    val tileProviders = LinkedList<TileProvider>()
    internal var mapName = "OpenMapFX Tiled Map"
    private val tileProvider = SimpleObjectProperty<TileProvider>()
    private val selectedTileType = SimpleObjectProperty<MapTileType>()

    init {
        val osmTileProvider = OSMTileProvider()
        tileProviders.add(osmTileProvider)
        tileProvider.set(osmTileProvider)
        selectedTileType.set(osmTileProvider.getDefaultType())

        if (System.getProperty("fileProvider") != null) {
            val fp = FileProvider(pName = "OSM local", baseUrl = System.getProperty("fileProvider"))
            tileProviders.add(fp)
        }
    }

    private var baseMap: MapArea? = null


    override fun getMapName(): String {
        return mapName
    }


    override fun getBaseMap(): BaseMap {
        if (baseMap == null) {
            baseMap = MapArea(selectedTileType)
        }

        return baseMap as MapArea
    }

    override fun getTileProviders(): List<TileProvider> {
        return tileProviders
    }

    override fun tileTypeProperty(): ObjectProperty<MapTileType> {
        return selectedTileType
    }

    override fun getTileTypes(): List<TileType> {
        return tileProvider.get().getTileTypes()
    }

    override fun toString(): String {
        return getMapName()
    }

    override fun tileProviderProperty(): ObjectProperty<TileProvider> {
        return tileProvider
    }
}
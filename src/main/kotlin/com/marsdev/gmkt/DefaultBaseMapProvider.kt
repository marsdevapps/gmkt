package com.marsdev.gmkt

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import java.util.*

class DefaultBaseMapProvider : BaseMapProvider {
    val tileProviders = LinkedList<TileProvider>()
    internal var mapName = "Tiled Map"
    private val tileProvider = SimpleObjectProperty<TileProvider>()
    private val selectedTileType = SimpleObjectProperty<MapTileType>()

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
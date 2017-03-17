package com.marsdev.gmkt.providers

import com.marsdev.gmkt.TileProvider
import com.marsdev.gmkt.TileType
import java.util.*

class OSMTileProvider : TileProvider {
    internal val providerName = "OpenStreetMap"
    private val tileTypes = LinkedList<TileType>()

    init {
        tileTypes.add(TileType("Map", "http://tile.openstreetmap.org/", "© OpenStreetMap contributors"))
    }

    override fun getProviderName(): String {
        return providerName
    }

    override fun getTileTypes(): List<TileType> {
        return tileTypes
    }

    override fun getDefaultType(): TileType {
        return tileTypes[0]
    }

    override fun getAttributionNotice(): String {
        return "© OpenStreetMap contributors"
    }

    override fun toString(): String {
        return getProviderName()
    }

}
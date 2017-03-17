package com.marsdev.gmkt.providers

import com.marsdev.gmkt.TileProvider
import com.marsdev.gmkt.TileType
import java.util.*

class FileProvider(val pName: String = "FileProvider", val copy: String = "open", val baseUrl: String) : TileProvider {

    private val tileTypes = LinkedList<TileType>()

    init {
        tileTypes.add(TileType(pName, baseUrl, copy))
    }

    override fun getProviderName(): String {
        return pName
    }

    override fun getTileTypes(): List<TileType> {
        return tileTypes
    }

    override fun getDefaultType(): TileType {
        return tileTypes[0]
    }

    override fun getAttributionNotice(): String {
        return copy
    }

    override fun toString(): String {
        return getProviderName()
    }

}
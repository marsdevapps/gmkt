package com.marsdev.gmkt

import javafx.concurrent.Worker
import javafx.scene.image.Image

interface TileType {

    /**
     * The display name for this style of map, for use in the user interface.

     * @return
     */
    val typeName: String

    /**
     * Returns the base URL for obtaining this type of tile from the tile provider.
     * For implementations that don't use a tile provider this can return null.

     * @return The base URL, ending in a forward slash so that zoom and location
     * * can be appended directly, or null.
     */
    val baseURL: String

    /**
     * An attribution for the tiles, to cover situations where copyright
     * requires displaying a notice based on the type of tile displayed.

     * @return
     */
    val attributionNotice: String

    fun getImage(zoom: Int, x: Long, y: Long): Worker<Image>

    fun setFileStorageBase(store: String)

}
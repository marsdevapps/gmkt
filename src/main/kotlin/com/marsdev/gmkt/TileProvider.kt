package com.marsdev.gmkt

interface TileProvider {
    /** Get the display name of the tile provider, for use in the UI.

     * @return The display name of the tile provider, e.g. "Map Quest"
     */
    fun getProviderName(): String

    /** Get an array of [TileType]s that this provider can supply.

     * @return  the list of tyletypes
     */
    fun getTileTypes(): List<TileType>

    /** Gets the default tile type for this provider, typically the map tile.

     * @return the default tiletype
     */
    fun getDefaultType(): TileType


    /** The attribution notice that is required by the tile provider to be
     * displayed.

     * @return Any legally required attribution notice that must be displayed.
     */
    fun getAttributionNotice(): String
}
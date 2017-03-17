package com.marsdev.gmkt

import javafx.beans.property.ObjectProperty

interface BaseMapProvider {
    /** A descriptive name to be used in any UI elements.

     * @return
     */
    fun getMapName(): String

    /** The BaseMap implementation that will be shown in the LayeredMap.

     * @return
     */
    fun getBaseMap(): BaseMap

    /** Supplies a potentially empty list of [TileProvider]s that can
     * supply tiles for this map.

     * @return
     */
    fun getTileProviders(): List<TileProvider>

    /**  Supported [MapTileType]s that can be shown by this type of map.

     * @return
     */
    fun getTileTypes(): List<MapTileType>

    /** A property that can be bound to a UI control then used to switch the
     * base map in the [LayeredMap]

     * @return
     */
    fun tileProviderProperty(): ObjectProperty<TileProvider>

    /** A property that can be bound to a UI control and then used to switch
     * the tile type in the [LayeredMap]

     * @return
     */
    fun tileTypeProperty(): ObjectProperty<MapTileType>
}
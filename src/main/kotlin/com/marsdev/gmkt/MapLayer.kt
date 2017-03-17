package com.marsdev.gmkt

import javafx.scene.Node

interface MapLayer {
    /**
     * Implementations must provide a Node that can be added to the view
     * by the LayeredMap implementation
     * @return the view for this specific MapLayer
     */
    fun getView(): Node

    /**
     * Callback method that will be called once this MapLayer is successfully
     * added to a LayeredMap
     * @param map the provided map.
     */
    fun gotLayeredMap(map: LayeredMap)
}
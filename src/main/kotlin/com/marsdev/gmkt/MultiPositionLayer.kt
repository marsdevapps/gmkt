package com.marsdev.gmkt

import javafx.scene.Node
import javafx.scene.Parent
import java.util.*

class MultiPositionLayer(var layeredMap: LayeredMap) : Parent(), MapLayer {

    private val nodePositions = HashMap<String, Position>()
    private val imageWidth: Double = 0.toDouble()
    private val imageHeight: Double = 0.toDouble()

    override fun getView(): Node {
        return this
    }

    /**
     * Adds a node to the layer. The id of the node is used internally to
     * uniquely identify it later when [updatePosition][.updatePosition]
     * is called. A random unique id will be set when no id was defined for the
     * node.
     * @param node the node to add
     * *
     * @param latitude the latitude coordinates of the initial position
     * *
     * @param longitude the longitude coordinates of the initial position
     */
    fun addNode(node: Node, latitude: Double, longitude: Double) {
        if (node.id == null) {
            node.id = UUID.randomUUID().toString()
        }

        nodePositions.put(node.id, Position(latitude, longitude))
        children.add(node)
    }

    /**
     * Removes the node from the layer.

     * @param node the node to remove
     */
    fun removeNode(node: Node) {
        nodePositions.remove(node.id)
        children.remove(node)
    }

    /**
     * Removes all nodes that were added to this layer.
     */
    fun removeAllNodes() {
        nodePositions.clear()
        children.clear()
    }

    /**
     * Updates the position of the node.

     * @param node the node to update
     * *
     * @param latitude the new latitude coordinates
     * *
     * @param longitude the new longitude coordinates
     */
    fun updatePosition(node: Node, latitude: Double, longitude: Double) {
        nodePositions.put(node.id, Position(latitude, longitude))
        refreshSingleLayer(node)
    }

    fun refreshEntireLayer() {
        for (node in children) {
            refreshSingleLayer(node)
        }
    }

    protected fun refreshSingleLayer(node: Node) {
        val nodePosition: Position = nodePositions[node.id] as Position
        if (nodePosition != null) {
            val cartPoint = this.layeredMap.getMapPoint(nodePosition.latitude, nodePosition.longitude)
            if (cartPoint == null) {
                println("[JVDBG] Null cartpoint, probably no scene, dont show.")
            } else {
                node.isVisible = true
                node.translateX = cartPoint.x - imageWidth / 2
                node.translateY = cartPoint.y - imageHeight / 2
            }
        } else {
            println("[JVDBG] Null nodePosition, probably updated a node position that was not added (node id: " + node.id + ".")
        }
    }

    override fun gotLayeredMap(map: LayeredMap) {
        this.layeredMap = map
        this.layeredMap.zoomProperty().addListener { e -> refreshEntireLayer() }
        this.layeredMap.centerLatitudeProperty().addListener { e -> refreshEntireLayer() }
        this.layeredMap.centerLongitudeProperty().addListener { e -> refreshEntireLayer() }
        this.layeredMap.xShiftProperty().addListener { e -> refreshEntireLayer() }
        this.layeredMap.yShiftProperty().addListener { e -> refreshEntireLayer() }
        refreshEntireLayer()
    }
}
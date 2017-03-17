package com.marsdev.gmkt

import javafx.scene.Node
import javafx.scene.Parent

class PositionLayer(val icon: Node, val iconTranslateX: Double, val iconTranslateY: Double) : Parent(), MapLayer {
    private var lat: Double = 0.0
    private var lon: Double = 0.0
    private var layeredMap: LayeredMap? = null

    init {
        icon.isVisible = false
        children.add(icon)
    }


    override fun getView(): Node {
        return this
    }

    fun updatePosition(lat: Double, lon: Double) {
        this.lat = lat
        this.lon = lon
        refreshLayer()
    }

    protected fun refreshLayer() {
        val cartPoint = this.layeredMap!!.getMapPoint(lat, lon)
        if (cartPoint == null) {
            println("[JVDBG] Null cartpoint, probably no scene, dont show.")
            return
        }
        this.icon.setVisible(true)
        this.icon.setTranslateX(cartPoint!!.getX() + iconTranslateX)
        this.icon.setTranslateY(cartPoint!!.getY() + iconTranslateY)
    }

    override fun gotLayeredMap(map: LayeredMap) {
        this.layeredMap = map
        this.layeredMap!!.zoomProperty().addListener({ e -> refreshLayer() })
        this.layeredMap!!.centerLatitudeProperty().addListener({ e -> refreshLayer() })
        this.layeredMap!!.centerLongitudeProperty().addListener({ e -> refreshLayer() })
        this.layeredMap!!.xShiftProperty().addListener({ e -> refreshLayer() })
        this.layeredMap!!.yShiftProperty().addListener({ e -> refreshLayer() })
        refreshLayer()
    }
}
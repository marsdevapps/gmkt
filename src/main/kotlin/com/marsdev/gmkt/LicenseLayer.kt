package com.marsdev.gmkt

import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane

class LicenseLayer(var provider: BaseMapProvider) : AnchorPane(), MapLayer {

    private val lblLicence = Label()
    private val tileProviderListener = { obs: ObservableValue<out TileProvider>, o: TileProvider, n: TileProvider -> updateLicence(n) }

    init {
        this.provider.tileProviderProperty().addListener(tileProviderListener)

        lblLicence.text = ""
        lblLicence.style = "-fx-background-color:rgba(66%,66%,66%,0.5)"

        AnchorPane.setLeftAnchor(lblLicence, 0.0)
        AnchorPane.setBottomAnchor(lblLicence, 0.0)
        //setRightAnchor(lblLicence, 0.0);

        children.add(lblLicence)

        updateLicence(provider.tileProviderProperty().get())
    }

    fun setBaseMapProvider(baseMapProvider: BaseMapProvider) {
        provider.tileProviderProperty().removeListener(tileProviderListener)
        provider = baseMapProvider
        provider.tileProviderProperty().addListener(tileProviderListener)
        updateLicence(provider.tileProviderProperty().get())
    }

    private fun updateLicence(tileProvider: TileProvider?) {
        if (tileProvider != null) {
            lblLicence.setText(tileProvider.getAttributionNotice())
        } else {
            lblLicence.setText("")
        }
    }

    override fun getView(): Node {
        return this
    }

    override fun gotLayeredMap(map: LayeredMap) {
        this.minWidthProperty().bind(map.widthProperty())
        this.minHeightProperty().bind(map.heightProperty())
    }
}
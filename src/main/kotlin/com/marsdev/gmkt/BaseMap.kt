package com.marsdev.gmkt

import javafx.beans.property.DoubleProperty
import javafx.geometry.Point2D
import javafx.scene.Node

interface BaseMap {

    /**
     * Moves the center of the map to the specified location
     * @param latitude The latitude in degrees
     * @param longitude The longitude in degrees
     */
    fun setCenter(latitude: Double, longitude: Double)

    /**
     * Move the center of the map horizontally by a number of pixels.
     *
     * @param dx the number of pixels
     */
    fun moveX(dx: Double)

    /**
     * Move the center of the map vertically by a number of pixels.
     *
     * @param dy the number of pixels
     */
    fun moveY(dy: Double)

    /**
     * Sets the map zoom level to the specified value.
     *
     * @param zoom the zoom level
     */
    fun setZoom(zoom: Double)

    /**
     * ?
     * @param deltat
     * @param pivotX
     * @param pivotY
     */
    fun zoom(delta: Double, pivotX: Double, pivotY: Double)

    fun zoomProperty(): DoubleProperty

    /**
     * @param latitude the latitude in degrees
     * @param longitude the longitude in degrees
     * @return The coordinate on the map that equates to the specified location
     */
    fun getMapPoint(latitude: Double, longitude: Double): Point2D

    /**
     * Return the geolocation(lat/lon) for a given point on the screen
     * @param sceneX
     * @param sceneY
     * @return
     */
    fun getMapPosition(sceneX: Double, sceneY: Double): Position

    /**
     * The current center longitude
     */
    fun centerLongitude(): DoubleProperty

    /**
     * The current center latitude
     */
    fun centerLatitude(): DoubleProperty

    /**
     * The UI component for this BaseMap.  This is the component that does the
     * actual visualization.
     *
     * @return the visual component of the BaseMap
     */
    fun getView(): Node

    /**
     * Called by the LayeredMap to allow the BaseMap to install any needed bindings
     * and listeners
     */
    fun install()

    /** Called by the LayerMap to allow the BaseMap to uninstall any
     * bindings and listeners.
     */
    fun uninstall()
}
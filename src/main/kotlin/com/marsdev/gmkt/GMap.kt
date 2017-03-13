package com.marsdev.gmkt

import javafx.beans.property.DoubleProperty
import javafx.geometry.Point2D
import javafx.scene.Node

interface GMap {

    /** Moves the center of the map to the specified location.

     * @param lat The latitude in degrees.
     * *
     * @param lon The longitude in degrees.
     */
    fun setCenter(lat: Double, lon: Double)

    /**
     * Move the center of the map horizontally by a number of pixels.

     * @param dx the number of pixels
     */
    fun moveX(dx: Double)

    /**
     * Move the center of the map vertically by a number of pixels.

     * @param dy the number of pixels
     */
    fun moveY(dy: Double)

    /** Sets the map zoom level to the specified value.

     * @param z
     */
    fun setZoom(z: Double)

    /**
     * @param delta
     * *
     * @param pivotX
     * *
     * @param pivotY
     */
    fun zoom(delta: Double, pivotX: Double, pivotY: Double)

    /**
     * @return
     */
    fun zoomProperty(): DoubleProperty

    /**
     * @param lat The latitude in degrees.
     * *
     * @param lon The longitude in degrees.
     * *
     * @return The coordinate on the map that equates to the specified location.
     */
    fun getMapPoint(lat: Double, lon: Double): Point2D

    /**
     * Return the geolocation (lat/lon) for a given point on the screen

     * @param sceneX
     * *
     * @param sceneY
     * *
     * @return
     */
    fun getMapPosition(sceneX: Double, sceneY: Double): MapPoint

    /** The current center longitude.

     * @return
     */
    fun centerLon(): DoubleProperty

    /** The current center latitude.

     * @return
     */
    fun centerLat(): DoubleProperty

    /**
     * The UI component for this BaseMap. This is the component that does the
     * actual visualization
     * @return  the visual component of the BaseMap
     */
    val mapView: Node

    /** Called by the LayerMap to allow the BaseMap to install any needed
     * bindings and listeners.
     */
    fun install()

    /** Called by the LayerMap to allow the BaseMap to uninstall any
     * bindings and listeners.
     */
    fun uninstall()

}
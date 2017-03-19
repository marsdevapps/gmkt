package com.marsdev.gmkt.providers

import com.marsdev.gmkt.TileProvider
import com.marsdev.gmkt.TileType
import java.util.*

class MapBoxTileProvider(val accessToken: String, val fileStorageBase: String) : TileProvider {
    internal val providerName = "MapBox"
    private val tileTypes = LinkedList<TileType>()
    private val tileTypesMap = HashMap<MapBoxTileType, TileType>()

    init {
        // create all of the map box types...
        val mbStreets = TileType("MapBox Streets", "http://api.mapbox.com/v4/mapbox.streets/", accessToken)
        mbStreets.setFileStorageBase(fileStorageBase + "mb-streets\\")

        val mbLight = TileType("MapBox Light", "http://api.mapbox.com/v4/mapbox.light/", accessToken)
        mbLight.setFileStorageBase(fileStorageBase + "mb-light\\")

        val mbDark = TileType("MapBox Dark", "http://api.mapbox.com/v4/mapbox.dark/", accessToken)
        mbDark.setFileStorageBase(fileStorageBase + "mb-dark\\")

        val mbSatellite = TileType("MapBox Satellite", "http://api.mapbox.com/v4/mapbox.satellite/", accessToken)
        mbSatellite.setFileStorageBase(fileStorageBase + "mb-satellite\\")

        val mbStreetsSatellite = TileType("MapBox Streets", "http://api.mapbox.com/v4/mapbox.streets-satellite/", accessToken)
        mbStreetsSatellite.setFileStorageBase(fileStorageBase + "mb-streets-satellite\\")

        val mbWheatpaste = TileType("MapBox Streets", "http://api.mapbox.com/v4/mapbox.wheatpaste/", accessToken)
        mbWheatpaste.setFileStorageBase(fileStorageBase + "mb-wheatepaste\\")

        val mbStreetsBasic = TileType("MapBox Streets Basic", "http://api.mapbox.com/v4/mapbox.streets-basic/", accessToken)
        mbStreetsBasic.setFileStorageBase(fileStorageBase + "mb-streets-basic\\")

        val mbComic = TileType("MapBox Comic", "http://api.mapbox.com/v4/mapbox.comic/", accessToken)
        mbComic.setFileStorageBase(fileStorageBase + "mb-comic\\")

        val mbOutdoors = TileType("MapBox Outdoors", "http://api.mapbox.com/v4/mapbox.outdoors/", accessToken)
        mbOutdoors.setFileStorageBase(fileStorageBase + "mb-outdoors\\")

        val mbRunBikeHike = TileType("MapBox Run Bike Hike", "http://api.mapbox.com/v4/mapbox.run-bike-hike/", accessToken)
        mbRunBikeHike.setFileStorageBase(fileStorageBase + "mb-run-bike-hike\\")

        val mbPencil = TileType("MapBox Comic", "http://api.mapbox.com/v4/mapbox.pencil/", accessToken)
        mbPencil.setFileStorageBase(fileStorageBase + "mb-pencil\\")

        val mbPirates = TileType("MapBox Pirates", "http://api.mapbox.com/v4/mapbox.pirates/", accessToken)
        mbPirates.setFileStorageBase(fileStorageBase + "mb-pirates\\")

        val mbEmerald = TileType("MapBox Emerald", "http://api.mapbox.com/v4/mapbox.emerald/", accessToken)
        mbEmerald.setFileStorageBase(fileStorageBase + "mb-emerald\\")

        val mbHighContrast = TileType("MapBox High Contrast", "http://api.mapbox.com/v4/mapbox.high-contrast/", accessToken)
        mbHighContrast.setFileStorageBase(fileStorageBase + "mb-high-contrast\\")

        tileTypes.add(mbStreets)
        tileTypesMap.put(MapBoxTileType.MAPBOX_STREETS, mbStreets)
        tileTypes.add(mbLight)
        tileTypesMap.put(MapBoxTileType.MAPBOX_LIGHT, mbLight)
        tileTypes.add(mbDark)
        tileTypesMap.put(MapBoxTileType.MAPBOX_DARK, mbDark)
        tileTypes.add(mbSatellite)
        tileTypesMap.put(MapBoxTileType.MAPBOX_SATELLITE, mbSatellite)
        tileTypes.add(mbStreetsSatellite)
        tileTypesMap.put(MapBoxTileType.MAPBOX_STREETS_SATELLITE, mbStreetsSatellite)
        tileTypes.add(mbWheatpaste)
        tileTypesMap.put(MapBoxTileType.MAPBOX_WHEATPASTE, mbWheatpaste)
        tileTypes.add(mbStreetsBasic)
        tileTypesMap.put(MapBoxTileType.MAPBOX_STREETS_BASIC, mbStreetsBasic)
        tileTypes.add(mbComic)
        tileTypesMap.put(MapBoxTileType.MAPBOX_COMIC, mbComic)
        tileTypes.add(mbOutdoors)
        tileTypesMap.put(MapBoxTileType.MAPBOX_OUTDOORS, mbOutdoors)
        tileTypes.add(mbRunBikeHike)
        tileTypesMap.put(MapBoxTileType.MAPBOX_RUN_BIKE_HIKE, mbRunBikeHike)
        tileTypes.add(mbPencil)
        tileTypesMap.put(MapBoxTileType.MAPBOX_PENCIL, mbPencil)
        tileTypes.add(mbPirates)
        tileTypesMap.put(MapBoxTileType.MAPBOX_PIRATES, mbPirates)
        tileTypes.add(mbEmerald)
        tileTypesMap.put(MapBoxTileType.MAPBOX_EMERALD, mbEmerald)
        tileTypes.add(mbHighContrast)
        tileTypesMap.put(MapBoxTileType.MAPBOX_HIGH_CONTRAST, mbHighContrast)
    }

    override fun getProviderName(): String {
        return providerName
    }

    override fun getTileTypes(): List<TileType> {
        return tileTypes
    }

    fun getTileType(mapBoxTileType: MapBoxTileType): TileType {
        if (tileTypesMap.containsKey(mapBoxTileType)) {
            return tileTypesMap[mapBoxTileType] as TileType
        } else {
            return tileTypes[0]
        }
    }

    override fun getDefaultType(): TileType {
        return tileTypes[0]
    }

    override fun toString(): String {
        return getProviderName()
    }

    override fun getAttributionNotice(): String {
        return "© MapBox"
    }
}

enum class MapBoxTileType {
    MAPBOX_STREETS,
    MAPBOX_LIGHT,
    MAPBOX_DARK,
    MAPBOX_SATELLITE,
    MAPBOX_STREETS_SATELLITE,
    MAPBOX_WHEATPASTE,
    MAPBOX_STREETS_BASIC,
    MAPBOX_COMIC,
    MAPBOX_OUTDOORS,
    MAPBOX_RUN_BIKE_HIKE,
    MAPBOX_PENCIL,
    MAPBOX_PIRATES,
    MAPBOX_EMERALD,
    MAPBOX_HIGH_CONTRAST
}

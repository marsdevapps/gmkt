package com.marsdev.gmkt.demo

import com.vividsolutions.jts.geom.Polygon
import org.geotools.data.DataUtilities
import org.geotools.data.simple.SimpleFeatureCollection
import org.geotools.kml.KML
import org.geotools.kml.KMLConfiguration
import org.geotools.xml.PullParser
import org.jsoup.Jsoup
import org.opengis.feature.simple.SimpleFeature
import java.io.File
import java.io.FileInputStream
import java.util.*

class OneLineReader {
    fun processBranches(featureCollection: SimpleFeatureCollection, buses: HashMap<Int, Bus>): HashSet<Branch> {
        val branches = HashSet<Branch>()
        with(featureCollection.features()) {
            while (hasNext()) {
                val simpleFeature = next()
                if (simpleFeature.id.startsWith("branch")) {
                    val branchBuses = simpleFeature.id.replace("branch", "").split("to")
                    branches.add(Branch(buses[branchBuses[0].toInt()], buses[branchBuses[1].toInt()]))
                }
            }
        }
        return branches
    }

    fun processBuses(featureCollection: SimpleFeatureCollection): HashMap<Int, Bus> {
        val buses = HashMap<Int, Bus>()

        with(featureCollection.features()) {
            while (hasNext()) {
                val simpleFeature = next()
                if (simpleFeature.id.startsWith("bus")) {

                    val htmlDescription = simpleFeature.getAttribute(5).toString()
                    var busName: String?
                    var busVoltage: Double?
                    var busId = simpleFeature.id.replace("bus", "").toInt()
                    val busArea: String?
                    val busZone: String?
                    val busSettlementZone: String?
                    val busSubstation: String?

                    val coordinate = (simpleFeature.getAttribute(9) as Polygon).centroid.coordinate

                    val descriptionMap = parseBusDescription(htmlDescription, busId)
                    busVoltage = descriptionMap["KV Level"]?.toDouble()
                    busArea = descriptionMap["Area"]
                    busZone = descriptionMap["Zone"]
                    busSettlementZone = descriptionMap["Settlement Zone"]
                    busSubstation = descriptionMap["Substation"]
                    busName = descriptionMap["Name"]
                    val bus = Bus(busName, busVoltage, busId, busArea, busZone, busSettlementZone, busSubstation, coordinate.y, coordinate.x)
                    buses.put(busId, bus)
                }
            }
        }

        return buses
    }

    fun parseBusDescription(description: String, busId: Int): HashMap<String, String> {
        val descriptionMap = HashMap<String, String>()
        val doc = Jsoup.parse(description)
        val tables = doc.getElementsByTag("table")
        if (tables.size == 2) {
            val table = tables[1]
            val rowsToFindColumn = table.select("tr")
            val rows = table.select("tr")

            var desiredColumn = 0
            rowsToFindColumn.forEach {
                var desiredColumnCounter = 1
                if (it.select("th").size == 1) {
                    if (it.select("th").text().equals("id", true)) {
                        val valueColsToCheck = it.select("td")
                        valueColsToCheck.forEach {
                            if (it.text().replace(",", "").toInt() == busId) {
                                desiredColumn = desiredColumnCounter
                            }
                        }
                    }
                }
            }


            rows.forEach {
                val headerCol = it.select("th")
                val valueCol = it.select("td")
                if (headerCol.size > 1) {
                    // first row is has all "th"..
                    descriptionMap.put("Name", headerCol[desiredColumn].text())
                } else {
                    descriptionMap.put(headerCol[0].text(), valueCol[desiredColumn - 1].text())
                }
            }
        }

        return descriptionMap
    }

    fun getPlacemarks(path: String): SimpleFeatureCollection {
        val source = File(path)
        val fis = FileInputStream(source)
        val parser = PullParser(KMLConfiguration(), fis, KML.Placemark)
        var simpleFeature: SimpleFeature?
        val features = ArrayList<SimpleFeature>()

        do {
            simpleFeature = parser.parse() as SimpleFeature?
            if (simpleFeature != null) {
                features.add(simpleFeature)
            }
        } while (
        simpleFeature != null
                )
        return DataUtilities.collection(features)
    }

}


data class Branch(var from: Bus?, var to: Bus?)
data class Bus(var name: String?, var voltage: Double?, var busId: Int?, var area: String?, var zone: String?, var settlementZone: String?, var substation: String?, var latitude: Double?, var longitude: Double?)

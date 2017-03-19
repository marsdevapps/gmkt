package com.marsdev.gmkt

import javafx.concurrent.Task
import javafx.concurrent.Worker
import javafx.scene.image.Image
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class TileType(val tName: String, val uRL: String, val attributionNotice: String) : MapTileType {
    private val debug = false

    lateinit private var typeName: String
    lateinit private var baseURL: String

    init {
        typeName = tName
        baseURL = uRL
    }

    private var cacheThread: CacheThread? = null

    override fun setFileStorageBase(store: String) {
        if (cacheThread != null) {
            cacheThread!!.deactivate()
            cacheThread = null
        }

        this.cacheThread = CacheThread(store)
        this.cacheThread!!.start()
    }

    override fun getImage(zoom: Int, i: Long, j: Long): Worker<Image> {
        val worker = object : Task<Image>() {
            @Throws(Exception::class)
            override fun call(): Image {
                val imageUrl = getImageURL(zoom, i, j)
                val bg = imageUrl.startsWith("http")
                return Image(getImageURL(zoom, i, j), bg)
            }
        }
        Thread(worker).start()
        return worker
    }

    override fun getTypeName(): String {
        return this.typeName
    }

    override fun getBaseURL(): String {
        return this.baseURL
    }

    protected fun getImageURL(zoom: Int, i: Long, j: Long): String {
        val cached = getFileCached(zoom, i, j)
        if (cached != null) {
            return cached
        } else {
            val url = calculateURL(zoom, i, j)
            if (cacheThread != null) {
                cacheThread!!.cacheImage(url, zoom, i, j)
            }
            return url
        }
    }

    private fun getFileCached(zoom: Int, i: Long, j: Long): String? {
        if (cacheThread != null) {
            return cacheThread!!.getCachedFile(zoom, i, j)
        }
        return null
    }

    protected fun calculateURL(zoom: Int, i: Long, j: Long): String {
        println(baseURL + zoom + "/" + i + "/" + j + ".png")
        return baseURL + zoom + "/" + i + "/" + j + ".png"
    }

    override fun toString(): String {
        return typeName
    }

    private class CacheThread(private val basePath: String) : Thread() {

        private var active = true
        private val offered = HashSet<String>()
        private val deque = LinkedBlockingDeque<String>()

        init {
            isDaemon = true
            name = "TileType CacheImagesThread"
        }

        fun deactivate() {
            this.active = false
        }

        override fun run() {
            while (active) {
                try {
                    val key = deque.pollFirst(10, TimeUnit.SECONDS)
                    if (key != null) {
                        val url = key.substring(0, key.lastIndexOf(";"))
                        val split = key.substring(key.lastIndexOf(";") + 1).split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val zoom = Integer.parseInt(split[0])
                        val i = java.lang.Long.parseLong(split[1])
                        val j = java.lang.Long.parseLong(split[2])
                        doCache(url, zoom, i, j)
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }

        fun cacheImage(url: String, zoom: Int, i: Long, j: Long) {
            val key = "$url;$zoom/$i/$j"
            synchronized(offered) {
                if (!offered.contains(key)) {
                    offered.add(key)
                    deque.offerFirst(key)
                }
            }
        }

        fun getCachedFile(zoom: Int, i: Long, j: Long): String? {
            val enc = File.separator + zoom + File.separator + i + File.separator + j + ".png"
            println("looking for " + enc + " in " + basePath)
            val candidate = File(basePath, enc)
            if (candidate.exists()) {
                println("FOUND " + enc + " in " + basePath)
                return candidate.toURI().toString()
            }
            return null
        }

        private fun doCache(urlString: String, zoom: Int, i: Long, j: Long) {
            try {
                val url = URL(urlString)
                println("Loading tile from URL " + urlString)
                url.openConnection().getInputStream().use { inputStream ->
                    val enc = File.separator + zoom + File.separator + i + File.separator + j + ".png"
                    val candidate = File(basePath, enc)
                    candidate.parentFile.mkdirs()
                    FileOutputStream(candidate).use { fos ->
                        val buff = ByteArray(4096)
                        var len = inputStream.read(buff)
                        while (len > 0) {
                            fos.write(buff, 0, len)
                            len = inputStream.read(buff)
                        }
                    }
                    println("Written tile from URL " + urlString + " to " + candidate)
                }
            } catch (ex: MalformedURLException) {
                Logger.getLogger(TileType::class.java.name).log(Level.SEVERE, null, ex)
            } catch (ex: IOException) {
                Logger.getLogger(TileType::class.java.name).log(Level.SEVERE, null, ex)
            }

        }
    }

}
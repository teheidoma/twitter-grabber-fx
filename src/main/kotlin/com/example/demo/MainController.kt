package com.example.demo

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import tornadofx.*
import twitter4j.MediaEntity
import twitter4j.Paging
import twitter4j.TwitterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

val twitterRegex = Regex("https?://twitter\\.com/(?<id>[^/\\W]+)")

class MainController : Controller() {
    val skipDuplicates = SimpleBooleanProperty()
    val url = SimpleStringProperty()
    val running = SimpleBooleanProperty()
    val total = SimpleIntegerProperty()
    val onlyVideo = SimpleBooleanProperty()
    val skipRetweets = SimpleBooleanProperty()

    val pool = Executors.newFixedThreadPool(5)
    val dir = File("media")
    val digests = HashSet<String>()


    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            pool.shutdownNow()
        })
        if (!dir.exists()) {
            dir.mkdir()
        }
    }


    fun start() {
        total.set(0)
        if (url.matches(twitterRegex).value) {
            val id = extractIdFromUrl()
            val idDir = File(dir, id)
            val twitter = TwitterFactory.getSingleton()

            if (!idDir.exists()) idDir.mkdir()
            println(id)
            running.set(true)

            runAsync {
                for (i in 1..10) {
                    if (!running.value) return@runAsync
                    println(i)
                    val page = Paging(i, 200)
                    val list = twitter.timelines().getUserTimeline(id, page)
                    if(list.size==0) break
                    list.filter { it.mediaEntities.isNotEmpty() }
                        .filter {
                            if (skipRetweets.get()) {
                                !it.isRetweet
                            } else {
                                true
                            }
                        }
                        .flatMap { it.mediaEntities.toList() }
                        .mapNotNull {
                            if (it.type == "video") {
                                it.videoVariants.maxBy(MediaEntity.Variant::getBitrate)!!.url
                            } else {
                                if (onlyVideo.not().value) {
                                    it.mediaURL
                                } else {
                                    null
                                }
                            }
                        }
                        .forEach { download(it, idDir) }
                }
            } ui {
                running.set(false)
            }


        }
    }

    @Synchronized
    private fun incrementTotal() {
        total.set(total.get() + 1)
    }

    val atomicInt = AtomicInteger()
    private fun download(url: String, dir: File) {
        if (!running.value) return

        CompletableFuture.runAsync(Runnable {
            val http = HttpClients.createDefault()
            val id = atomicInt.getAndIncrement()
            println("$id ${Thread.currentThread().name.split("-").last()} $url")
            val ext = if (url.contains(".mp4")) ".mp4" else ".jpg"
            http.execute(HttpGet(url)).use { response ->
                if (response.statusLine.statusCode == 200) {
                    val md = MessageDigest.getInstance("MD5")


                    val inputStream = DigestInputStream(response.entity.content, md)
                    val baos = ByteArrayOutputStream()


                    inputStream.copyTo(baos)

                    if (skipDuplicates.get()) {
                        val sum = String(md.digest())
                        if (digests.contains(sum)) {
                            println("found duplicate")
                        } else {
                            digests.add(sum)
                            val file = File(dir, id.toString() + ext)
                            file.outputStream().use {
                                baos.writeTo(it)
                            }
                        }
                    }else{
                        val file = File(dir, id.toString() + ext)
                        file.outputStream().use {
                            baos.writeTo(it)
                        }
                    }

                }
            }
            runLater {
                incrementTotal()
            }
        }, pool)
    }

    private fun extractIdFromUrl(): String {
        val matcher = twitterRegex.toPattern().matcher(url.value)
        matcher.find()
        return matcher.group("id")
    }

    fun cancel() {
        running.set(false)
    }
}
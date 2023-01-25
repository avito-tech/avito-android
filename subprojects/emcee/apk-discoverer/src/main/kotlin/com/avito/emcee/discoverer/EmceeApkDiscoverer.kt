package com.avito.emcee.discoverer

import com.avito.emcee.discoverer.gson.HttpUrlAdapter
import com.avito.emcee.discoverer.model.Request
import com.avito.emcee.discoverer.packagename.PackageNameResolver
import com.avito.emcee.discoverer.tests.TestDiscoverer
import com.google.gson.Gson
import io.ktor.serialization.gson.gson
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists

public object EmceeApkDiscoverer {

    private val gson: Gson = Gson()
    private val okHttpClient = OkHttpClient.Builder().build()
    private val downloader = BinaryFileDownloader(okHttpClient)
    private val packageNameResolver = PackageNameResolver()
    private val testDiscoverer = TestDiscoverer()

    @JvmStatic
    public fun main(args: Array<String>) {
        embeddedServer(Netty, 8080) {
            install(ContentNegotiation) {
                gson {
                    registerTypeAdapter(HttpUrl::class.java, HttpUrlAdapter())
                }
            }
            install(Routing) {
                post("/tests") {
                    val request: Request = call.receive()
                    val response = downloadAndDiscoverTests(request.apkUrl)
                    call.respondText(response)
                }
                post("/packagename") {
                    val request: Request = call.receive()
                    val response = downloadAndParsePackageName(request.apkUrl)
                    call.respondText(response)
                }
            }
        }.start(wait = true)
    }

    private fun downloadAndDiscoverTests(url: HttpUrl): String {
        val tmp = download(url)
        val list = testDiscoverer.discover(tmp)
        tmp.deleteIfExists()
        return gson.toJson(list)
    }

    private fun downloadAndParsePackageName(url: HttpUrl): String {
        val tmp = download(url)
        val result = packageNameResolver.resolve(tmp)
        tmp.deleteIfExists()
        return gson.toJson(result)
    }

    private fun download(url: HttpUrl): Path {
        val tmp: Path = createTempFile(UUID.randomUUID().toString(), ".apk")
        downloader.download(url, tmp.toFile())
        return tmp
    }
}

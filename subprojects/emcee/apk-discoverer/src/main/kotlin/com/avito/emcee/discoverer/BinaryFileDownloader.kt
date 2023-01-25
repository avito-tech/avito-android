package com.avito.emcee.discoverer

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

internal class BinaryFileDownloader(
    private val client: OkHttpClient,
) {

    fun download(url: HttpUrl, file: File) {
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = requireNotNull(response.body) { "Cannot download a file from $url. Response doesn't contain body." }
        body.byteStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}

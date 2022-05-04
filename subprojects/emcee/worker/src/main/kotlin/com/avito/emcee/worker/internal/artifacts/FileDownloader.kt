package com.avito.emcee.worker.internal.artifacts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import java.io.File

internal class FileDownloader(private val api: FileDownloaderApi) {

    private val fileNameProvider = FileNameResolver()

    suspend fun download(httpUrl: HttpUrl): File = withContext(Dispatchers.IO) {
        val fileName = fileNameProvider.resolve(httpUrl)
        val file = File(fileName).apply {
            if (exists()) delete()
            createNewFile()
        }

        api.downloadFile(httpUrl).run {
            require(isSuccessful && body() != null) { "Failed to download file from $httpUrl. $this" }
            val body = requireNotNull(body()) { "Response body of GET ($httpUrl) request is empty. $this" }

            body.byteStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        file
    }
}

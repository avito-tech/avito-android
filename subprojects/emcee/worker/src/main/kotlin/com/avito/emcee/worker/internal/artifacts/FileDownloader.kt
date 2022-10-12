package com.avito.emcee.worker.internal.artifacts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import java.io.File

internal class FileDownloader(private val api: FileDownloaderApi) {

    private val fileNameProvider = FileNameResolver()

    suspend fun download(httpUrl: HttpUrl): File = withContext(Dispatchers.IO) {
        val fileName = fileNameProvider.resolve(httpUrl)
        val file = File(fileName)
        if (file.exists()) {
            file.delete()
            file.createNewFile()
        }

        val downloadedFileResponse = api.downloadFile(httpUrl)
        require(downloadedFileResponse.isSuccessful) {
            "Failed to download file from $httpUrl. $downloadedFileResponse"
        }
        val body = requireNotNull(downloadedFileResponse.body()) {
            "Response body of GET ($httpUrl) request is empty. $downloadedFileResponse"
        }

        body.byteStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        file
    }
}

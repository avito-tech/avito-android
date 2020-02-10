package com.avito.filestorage

import com.avito.logger.Logger
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import java.io.File

interface RemoteStorage {

    companion object {

        fun create(
            endpoint: String,
            logger: Logger,
            httpClient: OkHttpClient = OkHttpClient()
        ): RemoteStorage = HttpRemoteStorage(
            endpoint = requireNotNull(HttpUrl.parse(endpoint)) ,
            httpClient = httpClient,
            logger = logger
        )
    }

    fun upload(
        uploadRequest: Request,
        comment: String,
        deleteOnUpload: Boolean = true
    ): FutureValue<Result>

    sealed class Request {

        sealed class FileRequest : Request() {

            abstract val file: File
            abstract val mediaType: MediaType

            data class Image(
                override val file: File,
                override val mediaType: MediaType = MediaType.parse("image/png")!!
            ) : FileRequest()

            data class Video(
                override val file: File,
                override val mediaType: MediaType = MediaType.parse("video/mp4")!!
            ) : FileRequest()
        }

        data class ContentRequest(val content: String, val extension: String) : Request()
    }

    sealed class Result {

        /**
         * @param url relative to the host
         */
        data class Success(
            val comment: String,
            val url: String,
            val timeInSeconds: Long,
            val uploadRequest: Request
        ) : Result()

        data class Error(
            val t: Throwable
        ) : Result()
    }
}

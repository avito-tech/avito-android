package com.avito.filestorage

import com.avito.logger.LoggerFactory
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import java.io.File

interface RemoteStorage {

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
                override val mediaType: MediaType = "image/png".toMediaType()
            ) : FileRequest()

            data class Video(
                override val file: File,
                override val mediaType: MediaType = "video/mp4".toMediaType()
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

    companion object {

        fun create(
            endpoint: String,
            loggerFactory: LoggerFactory,
            timeSource: TimeProvider = DefaultTimeProvider(),
            httpClient: OkHttpClient
        ): RemoteStorage = HttpRemoteStorage(
            endpoint = requireNotNull(endpoint.toHttpUrl()),
            httpClient = httpClient,
            loggerFactory = loggerFactory,
            timeSource = timeSource
        )
    }
}

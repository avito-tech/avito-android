package com.avito.filestorage

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import java.io.File

interface RemoteStorage {

    fun upload(
        uploadRequest: Request,
        comment: String,
        deleteOnUpload: Boolean = true
    ): FutureValue<Result>

    /**
     * если нужен полный путь до файла
     * при отрисовки Entity в ReportViewer это не требуется, т.к. это делает фронт
     */
    fun fullUrl(result: Result.Success): String

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

        sealed class ContentRequest : Request() {
            abstract val content: String
            abstract val extension: String

            data class Html(override val content: String) : ContentRequest() {
                override val extension: String = "html"
            }

            data class PlainText(override val content: String) : ContentRequest() {
                override val extension: String = "txt"
            }
        }
    }

    sealed class Result {

        abstract val comment: String
        abstract val timeInSeconds: Long
        abstract val uploadRequest: Request

        /**
         * @param url relative to the host
         */
        class Success(
            override val comment: String,
            override val timeInSeconds: Long,
            override val uploadRequest: Request,
            val url: String,
        ) : Result()

        class Error(
            override val comment: String,
            override val timeInSeconds: Long,
            override val uploadRequest: Request,
            val t: Throwable,
        ) : Result()
    }
}

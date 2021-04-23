package com.avito.filestorage

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import java.io.File

sealed class RemoteStorageRequest {

    sealed class FileRequest : RemoteStorageRequest() {

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

    sealed class ContentRequest : RemoteStorageRequest() {
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

package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.filestorage.ContentType
import com.avito.filestorage.RemoteStorage
import com.avito.report.model.Entry
import okhttp3.HttpUrl
import java.io.File

internal class AvitoFileStorageUploader(
    private val remoteStorage: RemoteStorage
) : TestArtifactsUploader {

    override suspend fun upload(content: String, type: Entry.File.Type): Result<HttpUrl> {
        return remoteStorage.upload(content, type.toContentType()).get()
    }

    override suspend fun upload(file: File, type: Entry.File.Type): Result<HttpUrl> {
        return remoteStorage.upload(file = file, type = type.toContentType()).get()
    }

    private fun Entry.File.Type.toContentType(): ContentType {
        return when (this) {
            Entry.File.Type.html -> ContentType.HTML
            Entry.File.Type.img_png -> ContentType.PNG
            Entry.File.Type.video -> ContentType.MP4
            Entry.File.Type.plain_text -> ContentType.TXT
        }
    }
}

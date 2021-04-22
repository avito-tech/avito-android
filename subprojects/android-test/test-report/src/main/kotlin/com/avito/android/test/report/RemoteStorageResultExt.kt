package com.avito.android.test.report

import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.report.model.Entry
import com.avito.report.model.FileAddress
import okhttp3.HttpUrl.Companion.toHttpUrl

internal fun List<FutureValue<RemoteStorage.Result>>.toEntries(): List<Entry.File> =
    map { it.toEntry() }

internal fun FutureValue<RemoteStorage.Result>.toEntry(): Entry.File {
    val result = get()
    return Entry.File(
        comment = result.comment,
        timeInSeconds = result.timeInSeconds,
        fileType = result.uploadRequest.toFileType(),
        fileAddress = result.fileAddress()
    )
}

private fun RemoteStorage.Request.toFileType(): Entry.File.Type {
    // false positive 'must be exhaustive' error in IDE,
    // should be fixed in kotlin 1.5 https://youtrack.jetbrains.com/issue/KT-44821
    return when (this) {
        is RemoteStorage.Request.ContentRequest.Html -> Entry.File.Type.html
        is RemoteStorage.Request.ContentRequest.PlainText -> Entry.File.Type.plain_text
        is RemoteStorage.Request.FileRequest.Image -> Entry.File.Type.img_png
        is RemoteStorage.Request.FileRequest.Video -> Entry.File.Type.video
    }
}

private fun RemoteStorage.Result.fileAddress(): FileAddress {
    // false positive 'must be exhaustive' error in IDE,
    // should be fixed in kotlin 1.5 https://youtrack.jetbrains.com/issue/KT-44821
    return when (this) {
        is RemoteStorage.Result.Success -> try {
            FileAddress.URL(url.toHttpUrl())
        } catch (e: IllegalArgumentException) {
            FileAddress.Error(e)
        }
        is RemoteStorage.Result.Error -> FileAddress.Error(t)
    }
}

package com.avito.upload_to_googleplay

import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.services.androidpublisher.AndroidPublisherRequest
import kotlin.math.roundToInt

/**
 * Copy-paste driven development
 * <a href="https://github.com/Triple-T/gradle-play-publisher/blob/master/plugin/src/main/kotlin/com/github/triplet/gradle/play/internal/Progress.kt">original</a>
 */
internal fun <T> AndroidPublisherRequest<T>.trackUploadProgress(
    thing: String
): AndroidPublisherRequest<T> {
    mediaHttpUploader?.apply {
        chunkSize = 4 * MediaHttpUploader.MINIMUM_CHUNK_SIZE // 1 MB
        setProgressListener {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (it.uploadState) {
                MediaHttpUploader.UploadState.INITIATION_STARTED ->
                    println("Starting $thing upload")
                MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS ->
                    println("Uploading $thing: ${(it.progress * 100).roundToInt()}% complete")
                MediaHttpUploader.UploadState.MEDIA_COMPLETE ->
                    println("${thing.capitalize()} upload complete")
            }
        }
    }
    return this
}

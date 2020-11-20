package com.avito.upload_to_googleplay

import com.avito.android.percent
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.services.androidpublisher.AndroidPublisherRequest

/**
 * Copy-paste driven development
 * <a href="https://github.com/Triple-T/gradle-play-publisher/blob/master/plugin/src/main/kotlin/com/github/triplet/gradle/play/internal/Progress.kt">original</a>
 */
internal fun <T> AndroidPublisherRequest<T>.trackUploadProgress(
    thing: String
): AndroidPublisherRequest<T> {
    mediaHttpUploader?.apply {
        chunkSize = CHUNK_SIZE
        setProgressListener {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (it.uploadState) {
                MediaHttpUploader.UploadState.INITIATION_STARTED ->
                    println("Starting $thing upload")
                MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS ->
                    println("Uploading $thing: ${it.progress.percent().toInt()}% complete")
                MediaHttpUploader.UploadState.MEDIA_COMPLETE ->
                    println("${thing.capitalize()} upload complete")
            }
        }
    }
    return this
}

/**
 * 1 mb
 */
private const val CHUNK_SIZE = 4 * MediaHttpUploader.MINIMUM_CHUNK_SIZE

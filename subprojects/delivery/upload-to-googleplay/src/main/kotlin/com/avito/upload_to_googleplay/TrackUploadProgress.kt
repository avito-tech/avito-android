package com.avito.upload_to_googleplay

import com.avito.capitalize
import com.avito.math.fromZeroToOnePercent
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.services.androidpublisher.AndroidPublisherRequest

internal fun <T> AndroidPublisherRequest<T>.trackUploadProgress(
    thing: String
): AndroidPublisherRequest<T> {
    mediaHttpUploader?.apply {
        chunkSize = CHUNK_SIZE
        setProgressListener {
            when (it.uploadState) {
                MediaHttpUploader.UploadState.INITIATION_STARTED ->
                    println("Starting $thing upload")
                MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS ->
                    println("Uploading $thing: ${it.progress.fromZeroToOnePercent().roundToInt()}% complete")
                MediaHttpUploader.UploadState.MEDIA_COMPLETE ->
                    println("${thing.capitalize()} upload complete")
                else -> {
                    // do nothing
                }
            }
        }
    }
    return this
}

/**
 * 1 mb
 */
private const val CHUNK_SIZE = 4 * MediaHttpUploader.MINIMUM_CHUNK_SIZE

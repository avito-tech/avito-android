package com.avito.android.test.report.video

import android.media.MediaMetadataRetriever
import java.io.File

internal interface VideoValidator {

    /**
     * @return true if at least one frame can be read from the video
     */
    fun isReadable(video: File): Boolean
}

internal class MediaMetadataVideoValidator : VideoValidator {

    override fun isReadable(video: File): Boolean {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(video.absolutePath)

            retriever.frameAtTime != null
        } catch (ignored: RuntimeException) {
            false
        } finally {
            retriever.release()
        }
    }
}

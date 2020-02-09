package com.avito.android.test.report.video

import android.annotation.SuppressLint
import android.util.Log
import com.avito.android.test.report.ReportState
import com.avito.android.test.report.listener.TestLifecycleListener
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.logger.Logger
import com.avito.report.model.Incident
import com.avito.report.model.Video
import okhttp3.OkHttpClient
import java.io.File

@SuppressLint("LogNotTimber")
class VideoCaptureTestListener(
    videoFeatureValue: VideoFeatureValue,
    onDeviceCacheDirectory: Lazy<File>,
    httpClient: OkHttpClient,
    fileStorageUrl: String,
    private val shouldRecord: Boolean,
    private val videoFeature: VideoFeature = VideoFeatureImplementation(videoFeatureValue),
    private val videoCapturer: VideoCapturer = VideoCapturerImplementation(onDeviceCacheDirectory)
) : TestLifecycleListener {

    private val logger: Logger = object : Logger {
        override fun debug(msg: String) {
            Log.d(LOG_TAG, msg)
        }

        override fun exception(msg: String, error: Throwable) {
            Log.d(LOG_TAG, msg, error)
        }

        override fun critical(msg: String, error: Throwable) {
            Log.d(LOG_TAG, msg, error)
        }

    }

    private val remoteStorage: RemoteStorage = RemoteStorage.create(
        logger = logger,
        httpClient = httpClient,
        endpoint = fileStorageUrl
    )

    private var savedIncident: Incident? = null

    override fun beforeTestStart(state: ReportState.Initialized.Started) {
        if (videoFeature.videoRecordingEnabled(shouldRecord)) {
            logger.debug("Video recording feature enabled. Recording starting")
            videoCapturer.start()
            logger.debug("Video recording feature enabled. Recording started")
        } else {
            logger.debug("Video recording feature disabled. Recording hasn't started")
        }
    }

    override fun afterIncident(incident: Incident) {
        savedIncident = incident
    }

    override fun afterTestStop(state: ReportState.Initialized.Started) {
        if (videoFeature.videoUploadingEnabled(shouldRecord, savedIncident)) {
            logger.debug("Video uploading enabled. Recording stopping...")
            val videoFile = videoCapturer.stop()
            logger.debug("Video uploading enabled. Recording stopped")

            if (videoFile != null) {
                val video = remoteStorage.upload(
                    uploadRequest = RemoteStorage.Request.FileRequest.Video(
                        file = videoFile
                    ),
                    comment = "video"
                )
                logger.debug("Video uploading enabled. Video uploaded")

                waitUploads(state = state, video = video)
            } else {
                logger.debug(
                    "Video uploading enabled. Filed to upload video for ${state.testMetadata.className}.${state.testMetadata.methodName}"
                )
            }
        } else {
            videoCapturer.abort()
            logger.debug("Video uploading disabled. Video recording process aborted")
        }
    }

    private fun waitUploads(
        state: ReportState.Initialized.Started,
        video: FutureValue<RemoteStorage.Result>
    ) {
        val videoUploadResult = video.get()

        if (videoUploadResult is RemoteStorage.Result.Success) {
            state.video = Video(
                link = videoUploadResult.url
            )
        }
    }
}

private const val LOG_TAG = "VideoCaptureListener"

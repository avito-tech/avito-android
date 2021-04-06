package com.avito.android.test.report.video

import com.avito.android.test.report.ReportState
import com.avito.android.test.report.listener.TestLifecycleListener
import com.avito.android.test.report.transport.ReportFileProvider
import com.avito.android.test.report.transport.Transport
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.Incident
import com.avito.report.model.Video

class VideoCaptureTestListener(
    videoFeatureValue: VideoFeatureValue,
    reportFileProvider: ReportFileProvider,
    loggerFactory: LoggerFactory,
    private val transport: Transport,
    private val shouldRecord: Boolean,
    private val videoFeature: VideoFeature = VideoFeatureImplementation(videoFeatureValue),
    private val videoCapturer: VideoCapturer = VideoCapturerImpl(reportFileProvider, loggerFactory)
) : TestLifecycleListener {

    private val logger = loggerFactory.create<VideoCaptureTestListener>()

    private var savedIncident: Incident? = null

    override fun beforeTestStart(state: ReportState.Initialized.Started) {
        if (videoFeature.videoRecordingEnabled(shouldRecord)) {
            logger.debug("Video recording feature enabled. Recording starting")
            videoCapturer.start().fold(
                onSuccess = { logger.debug("Video recording feature enabled. Recording started") },
                onFailure = { throwable ->
                    logger.warn(
                        "Video recording feature enabled. Failed to start recording.",
                        throwable
                    )
                }
            )
        } else {
            logger.debug("Video recording feature disabled.")
        }
    }

    override fun afterIncident(incident: Incident) {
        savedIncident = incident
    }

    override fun afterTestStop(state: ReportState.Initialized.Started) {
        if (videoFeature.videoUploadingEnabled(shouldRecord, savedIncident)) {
            logger.debug("Video uploading enabled. Recording stopping...")
            videoCapturer.stop().fold(
                onSuccess = { videoFile ->
                    logger.debug("Video uploading enabled. Recording stopped")
                    val video = transport.sendContent(
                        test = state.testMetadata,
                        request = RemoteStorage.Request.FileRequest.Video(videoFile),
                        comment = "video" // ignored, no field in report viewer
                    )
                    logger.debug("Video uploading enabled. Video uploaded")
                    waitUploads(state = state, video = video)
                },
                onFailure = { throwable ->
                    logger.warn(
                        "Video uploading enabled. " +
                            "Failed to upload video for " +
                            "${state.testMetadata.testName}.",
                        throwable
                    )
                }
            )
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

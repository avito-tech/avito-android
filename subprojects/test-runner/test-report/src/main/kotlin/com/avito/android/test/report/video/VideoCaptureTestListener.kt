package com.avito.android.test.report.video

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.listener.TestLifecycleListener
import com.avito.android.test.report.transport.Transport
import com.avito.filestorage.FutureValue
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.TestArtifactsProvider
import com.avito.report.model.Entry
import com.avito.report.model.Incident
import com.avito.report.model.Video

class VideoCaptureTestListener(
    videoFeatureValue: VideoFeatureValue,
    testArtifactsProvider: TestArtifactsProvider,
    loggerFactory: LoggerFactory,
    private val transport: Transport,
    private val shouldRecord: Boolean,
    private val videoFeature: VideoFeature = VideoFeatureImplementation(videoFeatureValue),
    private val videoCapturer: VideoCapturer = VideoCapturerImpl(testArtifactsProvider, loggerFactory)
) : TestLifecycleListener {

    private val logger = loggerFactory.create<VideoCaptureTestListener>()

    private var savedIncident: Incident? = null

    override fun beforeTestStart(state: Started) {
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

    override fun beforeTestFinished(state: Started) {
        if (videoFeature.videoUploadingEnabled(shouldRecord, savedIncident)) {
            logger.debug("Video uploading enabled. Recording stopping...")
            videoCapturer.stop().fold(
                onSuccess = { videoFile ->
                    logger.debug("Video uploading enabled. Recording stopped")
                    val video = transport.sendContent(
                        test = state.testMetadata,
                        file = videoFile,
                        type = Entry.File.Type.video,
                        comment = "video"
                    )
                    logger.debug("Video uploading enabled. Video uploaded")
                    waitUploads(state = state, video = video)
                },
                onFailure = { throwable ->
                    logger.warn(
                        "Video uploading enabled. " +
                            "Failed to upload video for " +
                            "${state.testMetadata.name}.",
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
        state: Started,
        video: FutureValue<Entry.File>
    ) {
        val videoUploadResult = video.get()
        state.video = Video(fileAddress = videoUploadResult.fileAddress)
    }
}

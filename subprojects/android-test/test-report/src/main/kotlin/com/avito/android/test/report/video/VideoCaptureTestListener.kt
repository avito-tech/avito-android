package com.avito.android.test.report.video

import com.avito.android.test.report.ReportState
import com.avito.android.test.report.listener.TestLifecycleListener
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.Incident
import com.avito.report.model.Video
import com.avito.time.TimeProvider
import okhttp3.OkHttpClient
import java.io.File

class VideoCaptureTestListener(
    videoFeatureValue: VideoFeatureValue,
    onDeviceCacheDirectory: Lazy<File>,
    httpClient: OkHttpClient,
    fileStorageUrl: String,
    loggerFactory: LoggerFactory,
    timeProvider: TimeProvider,
    private val shouldRecord: Boolean,
    private val videoFeature: VideoFeature = VideoFeatureImplementation(videoFeatureValue),
    private val videoCapturer: VideoCapturer = VideoCapturerImpl(onDeviceCacheDirectory, loggerFactory)
) : TestLifecycleListener {

    private val logger = loggerFactory.create<VideoCaptureTestListener>()

    private val remoteStorage: RemoteStorage = RemoteStorage.create(
        loggerFactory = loggerFactory,
        httpClient = httpClient,
        endpoint = fileStorageUrl,
        timeProvider = timeProvider
    )

    private var savedIncident: Incident? = null

    override fun beforeTestStart(state: ReportState.Initialized.Started) {
        if (videoFeature.videoRecordingEnabled(shouldRecord)) {
            logger.debug("Video recording feature enabled. Recording starting")
            when (val startingResult = videoCapturer.start()) {
                is VideoCapturer.StartingRecordResult.Success ->
                    logger.debug("Video recording feature enabled. Recording started")
                is VideoCapturer.StartingRecordResult.Error ->
                    logger.warn(
                        "Video recording feature enabled. Failed to start recording. " +
                            "Reason: ${startingResult.message}",
                        startingResult.error
                    )
            }
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
            when (val result = videoCapturer.stop()) {
                is VideoCapturer.RecordResult.Success -> {
                    logger.debug("Video uploading enabled. Recording stopped")
                    val video = remoteStorage.upload(
                        uploadRequest = RemoteStorage.Request.FileRequest.Video(
                            file = result.video
                        ),
                        comment = "video"
                    )
                    logger.debug("Video uploading enabled. Video uploaded")
                    waitUploads(state = state, video = video)
                }
                is VideoCapturer.RecordResult.Error ->
                    logger.warn(
                        "Video uploading enabled. " +
                            "Failed to upload video for " +
                            "${state.testMetadata.testName}. " +
                            "Reason: ${result.message}",
                        result.error
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

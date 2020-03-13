package com.avito.android.runner.delegates

import android.os.Bundle
import com.avito.android.runner.InstrumentationDelegate
import com.avito.android.runner.InstrumentationDelegateProvider
import com.avito.android.runner.TestRunEnvironment
import com.avito.android.test.report.listener.TestLifecycleNotifier
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.video.VideoCaptureTestListener
import com.avito.report.model.Kind
import okhttp3.OkHttpClient

class VideoCaptureDelegate(
    private val environment: TestRunEnvironment.RunEnvironment,
    private val httpClient: OkHttpClient
) : InstrumentationDelegate() {

    override fun beforeOnCreate(arguments: Bundle) {
        TestLifecycleNotifier.addListener(
            VideoCaptureTestListener(
                videoFeatureValue = environment.videoRecordingFeature,
                onDeviceCacheDirectory = environment.outputDirectory,
                httpClient = httpClient,
                shouldRecord = shouldRecordVideo(environment.testMetadata),
                fileStorageUrl = environment.fileStorageUrl
            )
        )
    }

    private fun shouldRecordVideo(testMetadata: TestMetadata): Boolean {
        return when (testMetadata.kind) {
            Kind.UI_COMPONENT, Kind.E2E -> true
            else -> false
        }
    }

    class Provider : InstrumentationDelegateProvider {
        override fun get(context: InstrumentationDelegateProvider.Context): InstrumentationDelegate {
            return VideoCaptureDelegate(
                environment = context.environment,
                httpClient = context.reportHttpClient
            )
        }
    }
}
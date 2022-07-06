package com.avito.emcee.client.di

import com.avito.android.TestSuiteLoader
import com.avito.emcee.client.EmceeTestClient
import com.avito.emcee.client.di.internal.FileUploaderProvider
import com.avito.emcee.client.di.internal.HttpClientProvider
import com.avito.emcee.client.internal.ArtifactorySettings
import com.avito.emcee.client.internal.EmceeTestClientImpl
import com.avito.emcee.client.internal.JobWaiter
import com.avito.emcee.client.internal.TestsParser
import com.avito.emcee.client.internal.UUIDBucketNameGenerator
import com.avito.emcee.queue.QueueApi
import com.avito.emcee.queue.QueueApi.Companion.create
import com.avito.logger.LoggerFactory
import retrofit2.Retrofit

public class EmceeTestActionFactory internal constructor(
    emceeQueueBaseUrl: String,
    fileUploaderProvider: FileUploaderProvider,
    httpClientProvider: HttpClientProvider
) {
    private val queueApi: QueueApi = Retrofit.Builder()
        .create(emceeQueueBaseUrl, httpClientProvider.provide())

    private val testsParser: TestsParser = TestsParser(TestSuiteLoader.create())

    private val fileUploader = fileUploaderProvider.provide(httpClientProvider.provide())

    public fun create(): EmceeTestClient {
        return EmceeTestClientImpl(queueApi, fileUploader, testsParser, JobWaiter(queueApi))
    }

    public companion object {
        public fun create(
            emceeQueueBaseUrl: String,
            artifactorySettings: ArtifactorySettings,
            loggerFactory: LoggerFactory
        ): EmceeTestActionFactory {
            return EmceeTestActionFactory(
                emceeQueueBaseUrl,
                FileUploaderProvider(artifactorySettings, UUIDBucketNameGenerator),
                HttpClientProvider(loggerFactory)
            )
        }
    }
}

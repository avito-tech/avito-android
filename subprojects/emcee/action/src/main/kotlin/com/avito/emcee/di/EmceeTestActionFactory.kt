package com.avito.emcee.di

import com.avito.android.TestSuiteLoader
import com.avito.emcee.EmceeTestAction
import com.avito.emcee.di.internal.FileUploaderProvider
import com.avito.emcee.di.internal.HttpClientProvider
import com.avito.emcee.internal.ArtifactorySettings
import com.avito.emcee.internal.EmceeTestActionImpl
import com.avito.emcee.internal.JobWaiter
import com.avito.emcee.internal.TestsParser
import com.avito.emcee.internal.UUIDBucketNameGenerator
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


    public fun create(): EmceeTestAction {
        return EmceeTestActionImpl(queueApi, fileUploader, testsParser, JobWaiter(queueApi))
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

package com.avito.emcee

import com.avito.android.TestSuiteLoader
import com.avito.emcee.internal.ArtifactoryFileUploader
import com.avito.emcee.internal.ArtifactorySettings
import com.avito.emcee.internal.BucketNameGenerator
import com.avito.emcee.internal.FileUploader
import com.avito.emcee.internal.TestsParser
import com.avito.emcee.internal.UUIDBucketNameGenerator
import com.avito.emcee.internal.createArtifactoryHttpClient
import com.avito.emcee.queue.QueueApi
import com.avito.emcee.queue.QueueApi.Companion.create
import retrofit2.Retrofit

public class EmceeTestActionFactory(
    emceeQueueBaseUrl: String,
    artifactorySettings: ArtifactorySettings,
) {
    private val bucketNameGenerator: BucketNameGenerator = UUIDBucketNameGenerator
    private val queueApi: QueueApi = Retrofit.Builder().create(emceeQueueBaseUrl)
    private val httpClient = createArtifactoryHttpClient(artifactorySettings)
    private val uploader: FileUploader = ArtifactoryFileUploader(
        httpClient = httpClient,
        artifactorySettings = artifactorySettings,
        bucketName = bucketNameGenerator.generate()
    )
    private val testsParser: TestsParser = TestsParser(TestSuiteLoader.create())

    public fun create(): EmceeTestAction {
        return EmceeTestAction(queueApi, uploader, testsParser)
    }
}

package com.avito.emcee

import com.avito.android.TestSuiteLoader
import com.avito.emcee.internal.ArtifactoryFileUploader
import com.avito.emcee.internal.FileUploader
import com.avito.emcee.internal.TestsParser
import com.avito.emcee.queue.QueueApi
import com.avito.emcee.queue.QueueApi.Companion.create
import retrofit2.Retrofit

public class EmceeTestActionFactory(emceeQueueBaseUrl: String) {
    private val queueApi: QueueApi = Retrofit.Builder().create(emceeQueueBaseUrl)
    private val uploader: FileUploader = ArtifactoryFileUploader()
    private val testsParser: TestsParser = TestsParser(TestSuiteLoader.create())

    public fun create(): EmceeTestAction {
        return EmceeTestAction(queueApi, uploader, testsParser)
    }
}

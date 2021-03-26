package com.avito.android.test.report

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.android.test.report.future.StubFutureValue
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.screenshot.ScreenshotUploader
import com.avito.android.test.report.troubleshooting.Troubleshooter
import com.avito.filestorage.HttpRemoteStorage
import com.avito.filestorage.RemoteStorage
import com.avito.logger.LoggerFactory
import com.avito.logger.StubLoggerFactory
import com.avito.report.model.Flakiness
import com.avito.report.model.Kind
import com.avito.time.TimeProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.mock.MockInterceptor
import okhttp3.mock.Rule
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File

class ReportTestExtension(
    val timeProvider: TimeProvider = mock(),
    private val fileStorageUrl: String = "https://filestorage.com",
    private val mockInterceptor: MockInterceptor = MockInterceptor(),
    private val screenshotUploader: ScreenshotUploader = mock(),
    private val loggerFactory: LoggerFactory = StubLoggerFactory,
    private val report: Report = ReportImplementation(
        onDeviceCacheDirectory = lazy { error("nope") },
        loggerFactory = loggerFactory,
        transport = emptyList(),
        screenshotUploader = screenshotUploader,
        timeProvider = timeProvider,
        remoteStorage = HttpRemoteStorage(
            endpoint = fileStorageUrl.toHttpUrl(),
            httpClient = OkHttpClient.Builder()
                .addInterceptor(mockInterceptor)
                .build(),
            timeProvider = timeProvider,
            loggerFactory = loggerFactory
        ),
        troubleshooter = NoOp
    )
) : BeforeEachCallback, Report by report {

    override fun beforeEach(context: ExtensionContext) {
        mockInterceptor.addRule(
            Rule.Builder()
                .post()
                .urlStarts(fileStorageUrl)
                .respond(200)
                .body(ResponseBody.run { "uri\":\"a\"".toResponseBody() })
        )
        whenever(screenshotUploader.makeAndUploadScreenshot(any()))
            .thenReturn(
                StubFutureValue(
                    RemoteStorage.Result.Error(
                        "stub screenshot",
                        0L,
                        RemoteStorage.Request.FileRequest.Image(File("stub")),
                        RuntimeException()
                    )
                )
            )
    }

    fun initTestCaseHelper(
        testCaseId: Int? = null,
        testClass: String = "com.avito.test.Test",
        testMethod: String = "test",
        testDescription: String? = null,
        dataSetNumber: Int? = null,
        kind: Kind = Kind.UNKNOWN,
        externalId: String? = null,
        tagIds: List<Int> = emptyList(),
        featureIds: List<Int> = emptyList(),
        priority: TestCasePriority? = null,
        behavior: TestCaseBehavior? = null,
        flakiness: Flakiness = Flakiness.Stable
    ) {
        initTestCase(
            testMetadata = TestMetadata(
                caseId = testCaseId,
                description = testDescription,
                className = testClass,
                methodName = testMethod,
                dataSetNumber = dataSetNumber,
                kind = kind,
                priority = priority,
                behavior = behavior,
                externalId = externalId,
                featureIds = featureIds,
                tagIds = tagIds,
                flakiness = flakiness
            )
        )
    }
}

private object NoOp : Troubleshooter {
    override fun troubleshootTo(report: Report) {
        // no op
    }
}

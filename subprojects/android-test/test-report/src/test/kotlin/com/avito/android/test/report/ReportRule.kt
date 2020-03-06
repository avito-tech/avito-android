package com.avito.android.test.report

import com.avito.android.rule.SimpleRule
import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.android.test.report.future.MockFutureValue
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.model.TestType
import com.avito.android.test.report.performance.PerformanceTestReporter
import com.avito.android.test.report.screenshot.ScreenshotUploader
import com.avito.android.test.report.transport.LocalRunTransport
import com.avito.filestorage.RemoteStorage
import com.avito.logger.Logger
import com.avito.report.model.DeviceName
import com.avito.report.model.Flakiness
import com.avito.report.model.Kind
import com.avito.report.model.ReportCoordinates
import com.avito.time.TimeProvider
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.sentry.SentryClientFactory
import okhttp3.OkHttpClient
import okhttp3.mock.MockInterceptor

/**
 * @param sendRealReport позволяет на время отладки посылать реальные репорты во время тестов,
 *                       чтобы посмотреть как оно отображается
 */
internal class ReportRule(
    // todo useless because report urls are empty
    val sendRealReport: Boolean = false,
    val mockTimeProvider: TimeProvider = mock(),
    private val mockInterceptor: MockInterceptor = MockInterceptor(),
    private val screenshotUploader: ScreenshotUploader = mock(),
    private val logger: Logger = object: Logger {
        override fun debug(msg: String) {
            println(msg)
        }

        override fun exception(msg: String, error: Throwable) {
            println(msg)
            error.printStackTrace()
        }

        override fun critical(msg: String, error: Throwable) {
            println(msg)
            error.printStackTrace()
        }

    },
    private val testRunCoordinates: ReportCoordinates = ReportCoordinates(
        planSlug = "android-test",
        jobSlug = "android-test",
        runId = "android-test5"
    ),
    private val deviceName: String = "android-test",
    private val report: Report = ReportImplementation(
        sentry = SentryClientFactory.sentryClient(), // TODO required?
        fileStorageUrl = "https://stub.ru", // TODO required?
        onDeviceCacheDirectory = lazy { error("nope") },
        httpClient = OkHttpClient.Builder()
            .apply {
                if (!sendRealReport) {
                    addInterceptor(mockInterceptor)
                }
            }
            .build(),
        performanceTestReporter = PerformanceTestReporter(),
        logger = logger,
        transport = if (sendRealReport) listOf(
            LocalRunTransport(
                reportApiHost = "", // TODO required?
                reportFallbackUrl = "", // TODO required
                reportViewerUrl = "", // TODO required
                reportCoordinates = testRunCoordinates,
                deviceName = DeviceName(deviceName),
                logger = logger
            )
        ) else emptyList()
    )
) : SimpleRule(), Report by report {

    override fun before() {
        whenever(screenshotUploader.makeAndUploadScreenshot(any()))
            .thenReturn(
                MockFutureValue(
                    RemoteStorage.Result.Error(
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
        testType: TestType = TestType.FUNCTIONAL,
        packageParserResult: TestPackageParser.Result = TestPackageParser.Result.Success(
            emptyList()
        ),
        externalId: String? = null,
        tagIds: List<Int> = emptyList(),
        featureIds: List<Int> = emptyList(),
        featuresFromAnnotation: List<String> = emptyList(),
        featuresFromPackage: List<String> = emptyList(),
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
                testType = testType,
                packageParserResult = packageParserResult,
                priority = priority,
                behavior = behavior,
                features = featuresFromPackage + featuresFromAnnotation,
                externalId = externalId,
                tagIds = tagIds,
                featureIds = featureIds,
                kind = kind,
                flakiness = flakiness
            )
        )
    }
}

package com.avito.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.FileAddress
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.Video
import com.avito.report.model.createStubInstance
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(StubReportsExtension::class)
internal class VideoTest {

    @Test
    fun `video - sent - with url`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testRuntimeData = TestRuntimeDataPackage.createStubInstance(
                    video = Video(FileAddress.URL("http://stub".toHttpUrl()))
                )
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.report.video.link", Matchers.equalTo("http://stub/")))
    }

    @Test
    fun `video - sent - as not uploaded file`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testRuntimeData = TestRuntimeDataPackage.createStubInstance(
                    video = Video(FileAddress.File("/tmp/some.file"))
                )
            )
        )
            .singleRequestCaptured().run {
                bodyMatches(hasNoJsonPath("$.params.report.video"))
                bodyMatches(
                    hasJsonPath(
                        "$.params.report.message_list",
                        Matchers.contains("Не удалось загрузить видео")
                    )
                )
            }
    }

    @Test
    fun `video - sent - as error`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testRuntimeData = TestRuntimeDataPackage.createStubInstance(
                    video = Video(FileAddress.Error(RuntimeException("Something went wrong")))
                )
            )
        )
            .singleRequestCaptured().run {
                bodyMatches(hasNoJsonPath("$.params.report.video"))
                bodyMatches(
                    hasJsonPath(
                        "$.params.report.message_list",
                        Matchers.contains("Не удалось загрузить видео: Something went wrong")
                    )
                )
            }
    }
}

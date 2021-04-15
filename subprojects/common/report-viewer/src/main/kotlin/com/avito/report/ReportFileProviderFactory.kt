package com.avito.report

import com.avito.report.internal.ReportFileProviderImpl
import com.avito.report.model.TestStaticData
import java.io.File

object ReportFileProviderFactory {

    fun create(
        testReportRootDir: Lazy<File>,
        testStaticData: TestStaticData
    ): ReportFileProvider {
        return ReportFileProviderImpl(
            rootDir = testReportRootDir,
            testDirGenerator = TestDirGenerator.StaticData(testStaticData)
        )
    }
}

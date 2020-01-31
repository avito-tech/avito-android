package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.report.model.DeviceName
import java.io.File

class FileTestsFilter(testsToRunFile: File?) : TestRunFilter {

    /**
     * Не проверяем на длину файла т.к переданный пустой файл является валидным знаком того, что не нужно ничего запускать.
     */
    private val testsToRun = if (testsToRunFile != null && testsToRunFile.exists()) testsToRunFile.readLines() else null

    override fun runNeeded(test: TestInApk, deviceName: DeviceName, api: Int): TestRunFilter.Verdict =
        if (testsToRun == null) {
            TestRunFilter.Verdict.Run
        } else {
            if (testsToRun.contains(test.testName.name)) {
                TestRunFilter.Verdict.Run
            } else {
                TestRunFilter.Verdict.Skip.NotSpecifiedInFile
            }
        }
}

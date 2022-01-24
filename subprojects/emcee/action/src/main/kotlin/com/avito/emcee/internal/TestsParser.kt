package com.avito.emcee.internal

import com.avito.android.Result
import com.avito.android.TestSuiteLoader
import com.avito.android.check.AllChecks
import com.avito.android.test.annotations.CaseId
import com.avito.android.test.annotations.TagId
import com.avito.emcee.queue.TestEntry
import com.avito.emcee.queue.TestName
import java.io.File

internal class TestsParser(
    private val suiteLoader: TestSuiteLoader
) {
    fun parse(
        testApkFile: File
    ): Result<List<TestEntry>> {
        return suiteLoader.loadTestSuite(testApkFile, AllChecks())
            .map { tests ->
                tests.map { test ->
                    TestEntry(
                        name = TestName(test.testName.className, test.testName.methodName),
                        caseId = test.annotations
                            .find { it.name == CaseId::class.java.canonicalName }
                            ?.getIntValue("value"),
                        tags = test.annotations
                            .find { it.name == TagId::class.java.canonicalName }
                            ?.getIntArrayValue("value")
                            ?.map { it.toString() } ?: emptyList(),
                    )
                }
            }
    }
}

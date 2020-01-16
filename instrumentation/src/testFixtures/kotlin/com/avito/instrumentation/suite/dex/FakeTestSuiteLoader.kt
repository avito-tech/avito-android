package com.avito.instrumentation.suite.dex

import com.avito.instrumentation.suite.dex.check.TestSignatureCheck
import java.io.File

class FakeTestSuiteLoader : TestSuiteLoader {

    val result = mutableListOf<TestInApk>()

    override fun loadTestSuite(
        file: File,
        testSignatureCheck: TestSignatureCheck?
    ): List<TestInApk> = result
}

package com.avito.instrumentation.suite.dex

import com.avito.instrumentation.suite.dex.check.TestSignatureCheck
import org.funktionale.tries.Try
import java.io.File

interface TestSuiteLoader {

    fun loadTestSuite(
        file: File,
        testSignatureCheck: TestSignatureCheck? = null
    ): Try<List<TestInApk>>
}

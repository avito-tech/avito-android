package com.avito.instrumentation.suite.dex

import com.avito.instrumentation.suite.dex.check.TestSignatureCheck
import org.funktionale.tries.Try
import java.io.File

class FakeTestSuiteLoader : TestSuiteLoader {

    val result = mutableListOf<TestInApk>()

    override fun loadTestSuite(
        file: File,
        testSignatureCheck: TestSignatureCheck?
    ): Try<List<TestInApk>> = Try.Success(result)
}

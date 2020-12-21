package com.avito.android

import com.avito.android.check.TestSignatureCheck
import org.funktionale.tries.Try
import java.io.File

class StubTestSuiteLoader : TestSuiteLoader {

    val result = mutableListOf<TestInApk>()

    override fun loadTestSuite(
        file: File,
        testSignatureCheck: TestSignatureCheck?
    ): Try<List<TestInApk>> = Try.Success(result)
}

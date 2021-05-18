package com.avito.android

import com.avito.android.check.TestSignatureCheck
import java.io.File

class StubTestSuiteLoader : TestSuiteLoader {

    val result = mutableListOf<TestInApk>()

    override fun loadTestSuite(
        file: File,
        testSignatureCheck: TestSignatureCheck?
    ): Result<List<TestInApk>> = Result.Success(result)
}

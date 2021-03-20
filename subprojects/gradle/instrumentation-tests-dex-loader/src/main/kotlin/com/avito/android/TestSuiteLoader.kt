package com.avito.android

import com.avito.android.check.TestSignatureCheck
import java.io.File

interface TestSuiteLoader {

    fun loadTestSuite(
        file: File,
        testSignatureCheck: TestSignatureCheck? = null
    ): Result<List<TestInApk>>
}

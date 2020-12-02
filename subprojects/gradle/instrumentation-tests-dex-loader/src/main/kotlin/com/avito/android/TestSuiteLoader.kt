package com.avito.android

import com.avito.android.check.TestSignatureCheck
import org.funktionale.tries.Try
import java.io.File

interface TestSuiteLoader {

    fun loadTestSuite(
        file: File,
        testSignatureCheck: TestSignatureCheck? = null
    ): Try<List<TestInApk>>
}

package com.avito.android

import com.avito.android.check.TestSignatureCheck
import java.io.File

public interface TestSuiteLoader {

    public fun loadTestSuite(
        file: File,
        testSignatureCheck: TestSignatureCheck? = null
    ): Result<List<TestInApk>>

    public companion object {

        public fun create(): TestSuiteLoader = TestSuiteLoaderImpl()
    }
}

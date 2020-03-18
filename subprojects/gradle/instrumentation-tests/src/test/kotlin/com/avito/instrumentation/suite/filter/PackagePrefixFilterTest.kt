package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.instrumentation.suite.dex.createStubInstance
import com.avito.report.model.DeviceName
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class PackagePrefixFilterTest {

    @Test
    fun `prefix filter - run - if empty prefix and any test`() {
        val isRunNeeded = PackagePrefixFilter("").runNeeded(
            TestInApk.createStubInstance(),
            deviceName = DeviceName("API22"),
            api = 22
        )

        assertThat(isRunNeeded).isInstanceOf(TestRunFilter.Verdict.Run::class.java)
    }

    @Test
    fun `prefix filter - run - if prefix matches test`() {
        val isRunNeeded = PackagePrefixFilter("com.avito.x.").runNeeded(
            TestInApk.createStubInstance(className = "com.avito.x.d"),
            deviceName = DeviceName("API22"),
            api = 22
        )

        assertThat(isRunNeeded).isInstanceOf(TestRunFilter.Verdict.Run::class.java)
    }

    @Test
    fun `prefix filter - run - if prefix doenst match test`() {
        val isRunNeeded = PackagePrefixFilter("com.avito.x.").runNeeded(
            TestInApk.createStubInstance(className = "com.avito.y.s"),
            deviceName = DeviceName("API22"),
            api = 22
        )

        assertThat(isRunNeeded).isInstanceOf(TestRunFilter.Verdict.Skip::class.java)
    }
}

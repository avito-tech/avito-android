package com.avito.android.build_checks

import org.junit.jupiter.api.Test

internal class RootProjectChecksExtensionTest {

    @Test
    fun `all default checks are enabled - default config`() {
        val extension = RootProjectChecksExtension()
        val checks = extension.enabledChecks()

        assertHasInstance<RootProjectChecksExtension.RootProjectCheck.MacOSLocalhost>(checks)
        assertHasInstance<RootProjectChecksExtension.RootProjectCheck.JavaVersion>(checks)
        assertHasInstance<RootProjectChecksExtension.RootProjectCheck.AndroidSdk>(checks)

        assertNoInstance<RootProjectChecksExtension.RootProjectCheck.GradleProperties>(checks)
    }
}

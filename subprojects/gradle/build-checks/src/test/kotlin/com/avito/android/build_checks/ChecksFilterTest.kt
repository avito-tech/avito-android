package com.avito.android.build_checks

import com.avito.android.build_checks.BuildChecksExtension.Check
import com.avito.android.build_checks.internal.ChecksFilter
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.Test

class ChecksFilterTest {

    @Test
    fun `no checks - enableByDefault is false`() {
        val extension = BuildChecksExtension().apply {
            enableByDefault = false
        }
        val checks = ChecksFilter(extension).enabledChecks()

        assertThat(checks).isEmpty()
    }

    @Test
    fun `all default checks are enabled - default config`() {
        val extension = BuildChecksExtension()
        val checks = ChecksFilter(extension).enabledChecks()

        assertHasInstance<Check.MacOSLocalhost>(checks)
        assertHasInstance<Check.DynamicDependencies>(checks)
        assertHasInstance<Check.GradleDaemon>(checks)
        assertHasInstance<Check.JavaVersion>(checks)
        assertHasInstance<Check.AndroidSdk>(checks)
        assertHasInstance<Check.UniqueRClasses>(checks)

        assertNoInstance<Check.GradleProperties>(checks)
        assertNoInstance<Check.ModuleTypes>(checks)
    }

    @Test
    fun `enable single check`() {
        val extension = BuildChecksExtension().apply {
            enableByDefault = false
            androidSdk { }
        }
        val checks = ChecksFilter(extension).enabledChecks()

        assertThat(checks).hasSize(1)
        assertHasInstance<Check.AndroidSdk>(checks)
    }

    @Test
    fun `disable single check`() {
        val extension = BuildChecksExtension().apply {
            androidSdk { it.enabled = false }
        }
        val checks = ChecksFilter(extension).enabledChecks()

        assertNoInstance<Check.AndroidSdk>(checks)

        assertWithMessage("Has other checks")
            .that(checks.size).isAtLeast(1)
    }

    // TODO: use comparingElementsUsing
    private inline fun <reified T> assertHasInstance(collection: Collection<Any>) {
        val count = collection.filterIsInstance<T>().size
        assertWithMessage(collection.joinToString() + " doesn't have " + T::class.java)
            .that(count).isEqualTo(1)
    }

    private inline fun <reified T> assertNoInstance(collection: Collection<Any>) {
        val count = collection.filterIsInstance<T>().size
        assertWithMessage(collection.joinToString() + " has " + T::class.java)
            .that(count).isEqualTo(0)
    }
}

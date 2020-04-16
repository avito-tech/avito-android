package com.avito.android.plugin.build_param_check

import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException

class ChecksFilterTest {

    @Test
    fun `all checks are enabled - legacy mode`() {
        val project = project(
            legacyEnabledGradleProperty to "true",

            "avito.build.failOnSdkMismatch" to "true",
            "avito.build.androidJar.revision" to 1
        )
        val checks = ChecksFilter(project, BuildChecksExtension()).checks()

        // this duplication will go away after deleting legacy mode
        assertHasInstance<Check.ModuleTypes>(checks)
        assertHasInstance<Check.GradleProperties>(checks)
        assertHasInstance<Check.MacOSLocalhost>(checks)
        assertHasInstance<Check.DynamicDependencies>(checks)
        assertHasInstance<Check.GradleDaemon>(checks)
        assertHasInstance<Check.JavaVersion>(checks)
        assertHasInstance<Check.AndroidSdk>(checks)
        assertHasInstance<Check.UniqueRClasses>(checks)

        checks.getInstance<Check.JavaVersion>().also { check ->
            assertThat(check.version).isEqualTo(org.gradle.api.JavaVersion.VERSION_1_8)
        }
        checks.getInstance<Check.AndroidSdk>().also { check ->
            assertThat(check.enabled).isEqualTo(true)
            assertThat(check.revision).isEqualTo(1)
        }
        checks.getInstance<Check.ModuleTypes>().also { check ->
            assertThat(check.enabled).isEqualTo(true)
        }
        checks.getInstance<Check.GradleProperties>().also { check ->
            assertThat(check.enabled).isEqualTo(true)
        }
    }

    @Test
    fun `no checks - enableByDefault is false`() {
        val extension = BuildChecksExtension().apply {
            enableByDefault = false
        }
        val checks = ChecksFilter(project(), extension).checks()

        assertThat(checks).isEmpty()
    }

    @Test
    fun `all default checks are enabled - default config`() {
        val extension = BuildChecksExtension()
        val checks = ChecksFilter(project(), extension).checks()

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
            androidSdk(Action { })
        }
        val checks = ChecksFilter(project(), extension).checks()

        assertThat(checks).hasSize(1)
        assertHasInstance<Check.AndroidSdk>(checks)
    }

    @Test
    fun `disable single check`() {
        val extension = BuildChecksExtension().apply {
            androidSdk(Action {
                it.enabled = false
            })
        }
        val checks = ChecksFilter(project(), extension).checks()

        assertNoInstance<Check.AndroidSdk>(checks)

        assertWithMessage("Has other checks")
            .that(checks.size).isAtLeast(1)
    }

    private fun project(vararg properties: Pair<String, Any>): Project {
        val project = ProjectBuilder.builder().build()
        properties.forEach { property ->
            project.extensions.add(property.first, property.second)
        }
        return project
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

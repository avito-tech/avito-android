package com.avito.android.build_checks

import com.avito.android.build_checks.BuildChecksExtensionTest.CustomBuildChecksExtension.CustomCheck.ConcreteCustomCheck
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.gradle.api.Action
import org.junit.jupiter.api.Test
import kotlin.reflect.full.createInstance

internal class BuildChecksExtensionTest {

    @Test
    fun `no checks - enableByDefault is false`() {
        val extension = CustomBuildChecksExtension().apply {
            enableByDefault = false
        }
        val checks = extension.enabledChecks()

        assertThat(checks).isEmpty()
    }

    @Test
    fun `enable single check`() {
        val extension = CustomBuildChecksExtension().apply {
            enableByDefault = false
            customCheck { }
        }
        val checks = extension.enabledChecks()

        assertThat(checks).hasSize(1)
        assertHasInstance<ConcreteCustomCheck>(checks)
    }

    @Test
    fun `disable single check`() {
        val extension = CustomBuildChecksExtension().apply {
            customCheck { it.enabled = false }
        }
        val checks = extension.enabledChecks()

        assertNoInstance<ConcreteCustomCheck>(checks)
    }

    private class CustomBuildChecksExtension : BuildChecksExtension() {

        override val allChecks: List<Check>
            get() {
                return CustomCheck::class.sealedSubclasses
                    .map { it.createInstance() }
            }

        fun customCheck(action: Action<ConcreteCustomCheck>): Unit =
            register(ConcreteCustomCheck(), action)

        sealed class CustomCheck : Check {

            override var enabled: Boolean = true

            open class ConcreteCustomCheck : CustomCheck()
        }
    }
}

// TODO: use comparingElementsUsing
internal inline fun <reified T> assertHasInstance(collection: Collection<Any>) {
    val count = collection.filterIsInstance<T>().size
    assertWithMessage(collection.joinToString() + " doesn't have " + T::class.java)
        .that(count).isEqualTo(1)
}

internal inline fun <reified T> assertNoInstance(collection: Collection<Any>) {
    val count = collection.filterIsInstance<T>().size
    assertWithMessage(collection.joinToString() + " has " + T::class.java)
        .that(count).isEqualTo(0)
}

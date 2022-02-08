package com.avito.android.build_checks

import org.gradle.api.Action

public abstract class BuildChecksExtension {

    public interface Check {

        public var enabled: Boolean
    }

    internal interface RequireValidation : Check {
        fun validate()
    }

    public var enableByDefault: Boolean = true

    private val registeredChecks = mutableSetOf<Check>()

    internal abstract val allChecks: List<Check>

    internal fun enabledChecks(): List<Check> {
        val enabledByUser = registeredChecks.filter { it.enabled }
        return if (enableByDefault) {
            enabledByUser + allChecksInDefaultState(excluded = registeredChecks).filter { it.enabled }
        } else {
            enabledByUser
        }
    }

    private fun allChecksInDefaultState(excluded: Set<Check>): List<Check> {
        return allChecks
            .filter {
                excluded.filterIsInstance(it::class.java).isEmpty()
            }
    }

    protected fun <T : Check> register(check: T, action: Action<T>) {
        @Suppress("UselessCallOnCollection")
        if (registeredChecks.filterIsInstance(check.javaClass).isNotEmpty()) {
            throw IllegalStateException(
                "You can't configure build check ${check.javaClass.simpleName} twice. " +
                    "See '$extensionName'."
            )
        }
        action.execute(check)
        registeredChecks.add(check)
    }
}

internal inline fun <reified T> Collection<Any>.hasInstance(): Boolean {
    return this.filterIsInstance(T::class.java).isNotEmpty()
}

internal inline fun <reified T> Collection<Any>.getInstance(): T {
    return this.filterIsInstance(T::class.java).first()
}

internal const val extensionName = "buildChecks"

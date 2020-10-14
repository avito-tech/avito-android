package com.avito.android.plugin.build_param_check

import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check
import kotlin.reflect.full.createInstance

internal class ChecksFilter(
    private val extension: BuildChecksExtension
) {

    fun enabledChecks(): List<Check> {
        val changedByUser = extension.checks
        val enabledByUser = changedByUser.filter { it.enabled }
        return if (extension.enableByDefault) {
            enabledByUser + allChecksInDefaultState(excluded = changedByUser).filter { it.enabled }
        } else {
            enabledByUser
        }
    }

    private fun allChecksInDefaultState(excluded: Set<Check>): List<Check> {
        return Check::class.sealedSubclasses
            .filter {
                excluded.filterIsInstance(it.java).isEmpty()
            }
            .map { it.createInstance() }
    }
}

internal inline fun <reified T> Collection<Any>.hasInstance(): Boolean {
    return this.filterIsInstance(T::class.java).isNotEmpty()
}

internal inline fun <reified T> Collection<Any>.getInstance(): T {
    return this.filterIsInstance(T::class.java).first()
}

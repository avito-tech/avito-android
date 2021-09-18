package com.avito.android.test

import org.gradle.api.tasks.testing.Test

/**
 * @return true if value is set
 */
@Suppress("unused")
fun Test.applyOptionalSystemProperty(name: String): Boolean {
    if (project.hasProperty(name)) {
        project.property(name)?.toString()?.let { value ->
            systemProperty(name, value)
            return true
        }
    }
    return false
}

/**
 * Try to set multiple system properties with names [name]
 * Calls [onMissing] with missing properties
 */
fun Test.applySystemProperties(vararg name: String, onMissing: (missing: Set<String>) -> Unit) {
    val result = name.associate { it to applyOptionalSystemProperty(it) }
    onMissing(result.filter { !it.value }.keys)
}

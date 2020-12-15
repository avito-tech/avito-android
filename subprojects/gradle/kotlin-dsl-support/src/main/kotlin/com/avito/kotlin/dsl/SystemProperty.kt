package com.avito.kotlin.dsl

/**
 * property can be passed through gradle
 *
 * see example in build.gradle.kts (Test task).systemProperty
 */
fun getSystemProperty(name: String, defaultValue: String? = null): String {
    val rawValue = System.getProperty(name)

    return if (rawValue.isNullOrBlank()) {
        if (defaultValue.isNullOrBlank()) {
            throw IllegalArgumentException("No value set for property: '$name', but it's required")
        } else {
            defaultValue
        }
    } else {
        rawValue
    }
}

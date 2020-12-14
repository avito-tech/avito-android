package com.avito.test.gradle

/**
 * property passed through gradle (Test task).systemProperty
 * see build.gradle.kts
 */
fun getTestProperty(name: String, defaultValue: String? = null): String {
    val rawValue = System.getProperty(name)

    return if (rawValue.isNullOrBlank()) {
        if (defaultValue.isNullOrBlank()) {
            throw IllegalArgumentException("No value set for test property: '$name', but it's required")
        } else {
            defaultValue
        }
    } else {
        rawValue
    }
}

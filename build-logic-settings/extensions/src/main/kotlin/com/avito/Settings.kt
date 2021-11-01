package com.avito

import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.extra

fun Settings.booleanProperty(name: String, defaultValue: Boolean): Boolean {
    return if (extra.has(name)) {
        extra[name]?.toString()?.toBoolean() ?: defaultValue
    } else {
        defaultValue
    }
}

fun Settings.mandatoryStringProperty(name: String): String {
    return if (settings.extra.has(name)) {
        val value = settings.extra[name]?.toString()
        if (value.isNullOrBlank()) {
            throw IllegalArgumentException("Property $name is required")
        } else {
            value
        }
    } else {
        throw IllegalArgumentException("Property $name is required")
    }
}

fun Settings.stringProperty(name: String, nullIfBlank: Boolean = false): String? {
    return if (extra.has(name)) {
        val string = extra[name]?.toString()
        if (nullIfBlank && string.isNullOrBlank()) null else string
    } else {
        null
    }
}

package com.avito.android.test

import org.gradle.api.tasks.testing.Test

@Suppress("unused")
fun Test.applyOptionalSystemProperty(name: String) {
    if (project.hasProperty(name)) {
        project.property(name)?.toString()?.let { value -> systemProperty(name, value) }
    }
}

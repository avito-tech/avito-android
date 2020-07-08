package com.avito.ci

import com.avito.test.gradle.TestResult
import com.google.common.truth.Truth.assertWithMessage
import okhttp3.mockwebserver.RecordedRequest

fun TestResult.assertAffectedModules(taskName: String, expectedModules: Set<String>) {
    assertThat().buildSuccessful()

    @Suppress("NAME_SHADOWING")
    val expectedModules = expectedModules
        .map { it.normalizeModuleName() }
        .toSet()

    val actualModules = output
        .split("\n")
        .filter { it.contains(":$taskName ") }
        .map { it.substringBefore(":$taskName ") }
        .toSet()


    assertWithMessage("Task $taskName has executed for modules: $expectedModules")
        .that(actualModules)
        .containsExactlyElementsIn(expectedModules)
}

fun RecordedRequest.bodyContains(body: CharSequence): Boolean {
    return this.body.toString().contains(body)
}

private fun String.normalizeModuleName() = if (this.startsWith(':')) this else ":$this"

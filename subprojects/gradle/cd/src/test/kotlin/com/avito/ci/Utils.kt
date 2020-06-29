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

fun detectChangedModules(
    projectDir: File,
    vararg args: String
): TestResult {
    return gradlew(projectDir, "generateModulesReport", *args)
}

fun TestResult.assertMarkedModules(
    projectDir: File,
    implementation: Set<String>,
    unitTests: Set<String>,
    androidTests: Set<String>
) {
    assertModulesInReport(File(projectDir, IMPLEMENTATION_MODULES_REPORT_PATH), implementation)
    assertModulesInReport(File(projectDir, UNIT_TEST_MODULES_REPORT_PATH), unitTests)
    assertModulesInReport(File(projectDir, ANDROID_TEST_MODULES_REPORT_PATH), androidTests)
}

@Suppress("NAME_SHADOWING")
private fun TestResult.assertModulesInReport(
    report: File,
    expectedModules: Set<String>
) {
    assertThat().buildSuccessful()

    val expectedModules = expectedModules
        .map { it.normalizeModuleName() }
        .toSet()

    val actualModules: Set<String> = report.readReport().toSet()

    Truth.assertWithMessage("Changed modules in ${report.name}")
        .that(actualModules)
        .containsExactlyElementsIn(expectedModules)
}

private fun String.normalizeModuleName() = if (this.startsWith(':')) this else ":$this"

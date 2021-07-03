package com.avito.impact

import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.google.common.truth.Truth.assertWithMessage
import java.io.File

public fun detectChangedModules(
    projectDir: File,
    vararg args: String
): TestResult {
    return gradlew(projectDir, "generateModulesReport", *args)
}

public fun TestResult.assertMarkedModules(
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

    assertWithMessage("Changed modules in ${report.name}")
        .that(actualModules)
        .containsExactlyElementsIn(expectedModules)
}

private fun File.readReport(): List<String> = readLines()
    .filterNot { it.isBlank() }
    .map { it.trim() }

private fun String.normalizeModuleName() = if (this.startsWith(':')) this else ":$this"

private const val IMPLEMENTATION_MODULES_REPORT_PATH = "build/reports/modules/implementation-modules.txt"
private const val UNIT_TEST_MODULES_REPORT_PATH = "build/reports/modules/unit-tests-modules.txt"
private const val ANDROID_TEST_MODULES_REPORT_PATH = "build/reports/modules/android-tests-modules.txt"

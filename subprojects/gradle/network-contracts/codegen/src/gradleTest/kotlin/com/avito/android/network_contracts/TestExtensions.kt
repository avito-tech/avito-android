package com.avito.android.network_contracts

import com.avito.test.gradle.TestResultSubject

internal fun TestResultSubject.outputContainsFilePaths(
    packageName: String,
    apiPath: String,
    paths: List<String>
): TestResultSubject {
    val packagePath = packageName.replace(".", "/")
    var result = this
    paths.forEach { result = result.outputContains("$packagePath$apiPath/$it") }
    return result
}

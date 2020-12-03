package com.avito.android

import com.avito.impact.changes.ChangeType
import com.avito.impact.changes.ChangesDetector
import com.avito.instrumentation.impact.KotlinClassesFinder
import org.funktionale.tries.Try
import java.io.File

class FindModifiedTestsAction(
    private val changesDetector: ChangesDetector,
    private val kotlinClassesFinder: KotlinClassesFinder
) {

    fun find(androidTestDir: File, allTestsInApk: List<TestInApk>): Try<List<String>> {
        return changesDetector.computeChanges(
            targetDirectory = androidTestDir,
            excludedDirectories = emptyList()
        ).map { changedFiles ->
            val changedClasses = changedFiles
                .filter { it.changeType == ChangeType.ADDED || it.changeType == ChangeType.MODIFIED }
                .flatMap { kotlinClassesFinder.find(it.file) }

            allTestsInApk
                .filter { changedClasses.contains(it.testName.className) }
                .map { it.testName.toString() }
        }
    }
}

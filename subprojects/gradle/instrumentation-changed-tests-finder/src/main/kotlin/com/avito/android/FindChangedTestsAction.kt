package com.avito.android

import com.avito.impact.changes.ChangeType
import com.avito.impact.changes.ChangesDetector
import com.avito.instrumentation.impact.KotlinClassesFinder
import com.avito.report.model.TestName
import org.funktionale.tries.Try
import java.io.File

class FindChangedTestsAction(
    private val changesDetector: ChangesDetector,
    private val kotlinClassesFinder: KotlinClassesFinder
) {

    /**
     * Change types considered as "Changed" in context of test execution strategies
     */
    private val changeTypes = arrayOf(
        ChangeType.ADDED,
        ChangeType.MODIFIED,
        ChangeType.COPIED
    )

    fun find(androidTestDir: File, allTestsInApk: List<TestName>): Try<List<String>> {
        return changesDetector.computeChanges(
            targetDirectory = androidTestDir,
            excludedDirectories = emptyList()
        ).map { changedFiles ->
            val changedClasses = changedFiles.asSequence()
                .filter { it.changeType in changeTypes }
                .flatMap { kotlinClassesFinder.findClasses(it.file) }
                .map { it.toString() }

            allTestsInApk
                .filter { changedClasses.contains(it.className) }
                .map { it.toString() }
        }
    }
}

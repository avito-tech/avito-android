package com.avito.android

import com.avito.impact.changes.ChangedFile
import com.avito.impact.changes.ChangesDetector
import org.funktionale.tries.Try
import java.io.File

class FindModifiedTestsAction(private val changesDetector: ChangesDetector) {

    fun find(androidTestDir: File, tests: List<TestInApk>): Try<List<String>> {
        return changesDetector.computeChanges(
            targetDirectory = androidTestDir,
            excludedDirectories = emptyList()
        ).map { changedFiles: List<ChangedFile> ->
            tests.map {  }
        }
    }
}

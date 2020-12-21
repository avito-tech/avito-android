package com.avito.impact.changes

import org.funktionale.tries.Try
import java.io.File

class StubChangesDetector : ChangesDetector {

    lateinit var result: Try<List<ChangedFile>>

    override fun computeChanges(
        targetDirectory: File,
        excludedDirectories: Iterable<File>
    ): Try<List<ChangedFile>> = result
}

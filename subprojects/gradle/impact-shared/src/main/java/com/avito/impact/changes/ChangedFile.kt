package com.avito.impact.changes

import java.io.File

public data class ChangedFile(
    val rootDir: File,
    val file: File,
    val changeType: ChangeType
) {

    init {
        require(file.startsWith(rootDir)) { "File $file must be within $rootDir" }
    }

    public val relativePath: String = file.toRelativeString(rootDir)
}

package com.avito.android.async_tc_cleaner.internal.reaper

import java.nio.file.Path
import java.nio.file.SecureDirectoryStream

internal class StagedDir(
    val stagingDirStream: SecureDirectoryStream<Path>,
    val stagedEntryRelativePath: Path,
    val reportedPath: Path,
) {
    init {
        val raw = stagedEntryRelativePath.toString()
        require(
            !stagedEntryRelativePath.isAbsolute
                && stagedEntryRelativePath.nameCount == 1
                && raw != "."
                && raw != ".."
        ) {
            "Staged entry relative path must be a single path component, got '$raw'"
        }
    }
}

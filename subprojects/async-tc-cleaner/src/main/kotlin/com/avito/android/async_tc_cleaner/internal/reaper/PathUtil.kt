package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.Result
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.SecureDirectoryStream

internal fun Path.directoryStream(): Result<SecureDirectoryStream<Path>> = Result.tryCatch {
    // We have to use parent directory stream here because Files.newDirectoryStream API doesn't have LinkOption params
    val parentDir = parent ?: return@tryCatch openSecureDirectoryStream(this)
    openSecureDirectoryStream(parentDir).use { parentDirStream ->
        parentDirStream.newDirectoryStream(fileName, LinkOption.NOFOLLOW_LINKS)
    }
}

private fun openSecureDirectoryStream(dir: Path): SecureDirectoryStream<Path> {
    val directoryStream = Files.newDirectoryStream(dir)
    @Suppress("UNCHECKED_CAST")
    return directoryStream as? SecureDirectoryStream<Path>
        ?: error("Filesystem does not support secure directory streams: $dir")
}

internal fun reportedPath(parentPath: Path?, entryRelativePath: Path): Path =
    parentPath?.resolve(entryRelativePath) ?: entryRelativePath

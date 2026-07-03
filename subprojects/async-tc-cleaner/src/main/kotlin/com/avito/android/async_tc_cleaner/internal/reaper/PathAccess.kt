package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.Result
import java.io.IOException
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path

internal fun Path.directoryStream(): Result<DirectoryStream<Path>> = try {
    Result.Success(Files.newDirectoryStream(this))
} catch (e: IOException) {
    Result.Failure(e)
}

package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.Result
import java.nio.file.Path

internal interface TreeDeleter {
    suspend fun delete(root: Path): Result<DeletionOutcome>
}

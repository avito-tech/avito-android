package com.avito.android.async_tc_cleaner.internal.reaper

internal interface TreeDeleter {
    suspend fun delete(stagedDir: StagedDir): DeletionOutcome
}

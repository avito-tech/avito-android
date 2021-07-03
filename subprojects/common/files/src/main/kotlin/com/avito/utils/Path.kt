package com.avito.utils

import androidx.annotation.RequiresApi
import com.avito.android.Result
import java.nio.file.Path

@RequiresApi(api = 26)
public fun Path.deleteRecursively(): Result<Unit> {
    return Result.tryCatch {
        val isDeleted = toFile().deleteRecursively()
        if (!isDeleted) {
            throw IllegalStateException("not cleared, reason unknown because of java.io.File API")
        }
    }
}

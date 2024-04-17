package com.avito.git.executor

import com.avito.android.Result

internal interface GitExecutor {

    fun git(command: String): Result<String>
}

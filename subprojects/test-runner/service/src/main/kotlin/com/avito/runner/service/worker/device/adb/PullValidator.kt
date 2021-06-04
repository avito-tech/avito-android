package com.avito.runner.service.worker.device.adb

import com.avito.android.Problem
import java.nio.file.Path

interface PullValidator {

    fun isPulledCompletely(hostDir: Path): Result

    sealed class Result {

        object Ok : Result()

        data class Failure(val problem: Problem) : Result()
    }
}

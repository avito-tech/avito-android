package com.avito.runner.service.worker.device.adb

import com.avito.android.Problem
import java.nio.file.Path

public interface PullValidator {

    public fun isPulledCompletely(hostDir: Path): Result

    public sealed class Result {

        public object Ok : Result()

        public data class Failure(val problem: Problem) : Result()
    }
}

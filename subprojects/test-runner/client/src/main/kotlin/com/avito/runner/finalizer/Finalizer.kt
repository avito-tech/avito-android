package com.avito.runner.finalizer

import com.avito.runner.scheduler.runner.scheduler.TestsScheduler

public interface Finalizer {

    public fun finalize(testSchedulerResults: TestsScheduler.Result): Result

    public sealed class Result {

        public object Ok : Result()

        public data class Failure(val message: String) : Result()
    }
}

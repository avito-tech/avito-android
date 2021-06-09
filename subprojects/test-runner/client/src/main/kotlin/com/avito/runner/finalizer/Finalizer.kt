package com.avito.runner.finalizer

import com.avito.runner.scheduler.runner.model.TestSchedulerResult

public interface Finalizer {

    public fun finalize(testSchedulerResults: TestSchedulerResult): Result

    public sealed class Result {

        public object Ok : Result()

        public data class Failure(val message: String) : Result()
    }
}

package com.avito.runner.scheduler.runner.scheduler

public sealed class TestSchedulerResult {

    public object Ok : TestSchedulerResult()

    public data class Failure(val message: String) : TestSchedulerResult()
}

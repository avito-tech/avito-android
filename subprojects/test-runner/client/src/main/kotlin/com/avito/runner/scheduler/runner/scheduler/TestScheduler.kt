package com.avito.runner.scheduler.runner.scheduler

import com.avito.runner.scheduler.runner.model.TestSchedulerResult

public interface TestScheduler {

    public fun schedule(): TestSchedulerResult
}

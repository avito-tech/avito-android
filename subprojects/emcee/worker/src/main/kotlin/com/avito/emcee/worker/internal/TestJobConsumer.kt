package com.avito.emcee.worker.internal

import kotlinx.coroutines.flow.Flow

internal interface TestJobConsumer {

    data class Result(val results: List<TestExecutor.Result>)

    fun consume(jobs: Flow<TestJobProducer.Job>): Flow<Result>
}

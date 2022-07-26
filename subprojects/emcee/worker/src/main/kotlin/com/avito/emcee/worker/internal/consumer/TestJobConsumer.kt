package com.avito.emcee.worker.internal.consumer

import com.avito.emcee.worker.internal.TestExecutor
import com.avito.emcee.worker.internal.TestJobProducer
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal interface TestJobConsumer {

    data class Result(val results: List<TestExecutor.Result>)

    fun consume(jobs: Flow<TestJobProducer.Job>): Flow<Result>
}

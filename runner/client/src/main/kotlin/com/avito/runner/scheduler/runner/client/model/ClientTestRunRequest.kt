package com.avito.runner.scheduler.runner.client.model

import com.avito.runner.scheduler.runner.scheduler.TestExecutionState
import com.avito.runner.service.model.intention.Intention

data class ClientTestRunRequest(
    val state: TestExecutionState,
    val intention: Intention
)

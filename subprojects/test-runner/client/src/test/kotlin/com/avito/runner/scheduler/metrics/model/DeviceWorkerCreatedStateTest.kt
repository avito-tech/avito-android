package com.avito.runner.scheduler.metrics.model

import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

internal class DeviceWorkerCreatedStateTest {

    private val state = DeviceWorkerState.Created(
        created = Instant.now(),
        testExecutionStates = ConcurrentHashMap.newKeySet(),
        key = DeviceCoordinate.Local.createStubInstance()
    )

    @Test
    fun `test executed - success`() {
        state.testIntentionReceived(TestKey.createStubInstance(), Instant.now())
        state.testStarted(TestKey.createStubInstance(), Instant.now())
        state.testCompleted(TestKey.createStubInstance(), Instant.now())
        state.finish(Instant.now())
    }

    @Test
    fun `first test intentionReceived second test started - IllegalStateException`() {
        val error = assertThrows<IllegalStateException> {
            state.testIntentionReceived(TestKey.createStubInstance(executionNumber = 1), Instant.now())
            state.testStarted(TestKey.createStubInstance(executionNumber = 2), Instant.now())
        }

        assertThat(error.message)
            .isEqualTo("Can't find IntentionReceived for TestKey(test=com.avito.Test.test.api22, executionNumber=2)")
    }

    @Test
    fun `testIntentionReceived twice - IllegalStateException`() {
        val error = assertThrows<IllegalStateException> {
            state.testIntentionReceived(TestKey.createStubInstance(), Instant.now())
            state.testIntentionReceived(TestKey.createStubInstance(), Instant.now())
        }

        @Suppress("MaxLineLength")
        assertThat(error.message)
            .isEqualTo("Intention TestKey(test=com.avito.Test.test.api22, executionNumber=0) already has been received")
    }

    @Test
    fun `testStarted before testIntentionReceived - IllegalStateException`() {
        val error = assertThrows<IllegalStateException> {
            state.testStarted(TestKey.createStubInstance(), Instant.now())
        }

        assertThat(error.message)
            .isEqualTo("Can't find IntentionReceived for TestKey(test=com.avito.Test.test.api22, executionNumber=0)")
    }

    @Test
    fun `testCompleted before testStarted - IllegalStateException`() {
        val error = assertThrows<IllegalStateException> {
            state.testCompleted(TestKey.createStubInstance(), Instant.now())
        }

        assertThat(error.message)
            .isEqualTo("Can't find Started for TestKey(test=com.avito.Test.test.api22, executionNumber=0)")
    }
}

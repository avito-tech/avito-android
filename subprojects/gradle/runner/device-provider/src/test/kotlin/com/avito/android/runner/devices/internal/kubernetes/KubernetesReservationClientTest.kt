package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.FakeAndroidDebugBridge
import com.avito.android.runner.devices.internal.StubEmulatorsLogsReporter
import com.avito.android.runner.devices.model.ReservationData
import com.avito.android.runner.devices.model.stub
import com.avito.logger.StubLoggerFactory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.LinkedList

@ExperimentalCoroutinesApi
internal class KubernetesReservationClientTest {

    private val kubernetesApi = FakeKubernetesApi()
    private val dispatcher = TestCoroutineDispatcher()
    private fun runBlockingTest(block: suspend TestCoroutineScope.() -> Unit) {
        dispatcher.runBlockingTest(block)
    }

    private fun client(dispatcher: CoroutineDispatcher = this.dispatcher): KubernetesReservationClient {
        return KubernetesReservationClient(
            androidDebugBridge = FakeAndroidDebugBridge(),
            kubernetesApi = kubernetesApi,
            emulatorsLogsReporter = StubEmulatorsLogsReporter,
            loggerFactory = StubLoggerFactory,
            reservationDeploymentFactory = FakeReservationDeploymentFactory(),
            dispatcher = dispatcher,
            podsQueryIntervalMs = 1L
        )
    }

    @Test
    fun `empty reservations - throws exception`() {
        val client = client()
        val exception = assertThrows<IllegalArgumentException> {
            runBlockingTest {
                client.claim(emptyList(), this)
            }
        }
        assertThat(exception.message)
            .isEqualTo("Must have at least one reservation but empty")
    }

    @Test
    fun `creating deployment fail - throws exception`() {
        val client = client()
        val message = "Failed to create deployment"
        kubernetesApi.createDeployment = { throw RuntimeException(message) }
        val exception = assertThrows<RuntimeException>(message = message) {
            runBlockingTest {
                client.claim(listOf(ReservationData.stub()), this)
            }
        }
        assertThat(exception.message)
            .isEqualTo(message)
    }

    @Test
    fun `claim twice - throws exception`() {
        val client = client()
        val exception = assertThrows<IllegalStateException> {
            runBlockingTest {
                // first
                client.claim(listOf(ReservationData.stub()), this)

                // second
                client.claim(listOf(ReservationData.stub()), this)
            }
        }
        assertThat(exception.message)
            .isEqualTo("Unable to start reservation job. Already started")
    }

    @Test
    fun `get one pod then empty - success`() {
        val client = client()
        val results = LinkedList(
            listOf(
                listOf(StubPod()),
                emptyList()
            ),
        )

        kubernetesApi.getPods = {
            results.poll()
        }
        dispatcher.runBlockingTest {
            client.claim(listOf(ReservationData.stub()), this)
        }
    }

    @Test
    fun `devices channel cancel - success`() {
        val client = client(dispatcher = Dispatchers.Default)
        kubernetesApi.getPods = {
            listOf(StubPod())
        }
        runBlocking {
            val result = client.claim(listOf(ReservationData.stub()), this)
            result.deviceCoordinates.cancel()
        }
    }

    @Test
    fun `claim then release - success`() {
        val client = client(dispatcher = Dispatchers.Default)
        kubernetesApi.getPods = {
            listOf(StubPod())
        }
        runBlocking {
            client.claim(listOf(ReservationData.stub()), this)
            client.release()
        }
    }
}

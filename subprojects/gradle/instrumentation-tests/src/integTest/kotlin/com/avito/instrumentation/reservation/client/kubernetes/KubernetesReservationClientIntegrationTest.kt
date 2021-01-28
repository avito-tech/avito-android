package com.avito.instrumentation.reservation.client.kubernetes

import com.avito.instrumentation.internal.reservation.client.ReservationClient
import com.avito.instrumentation.internal.reservation.client.kubernetes.KubernetesReservationClient
import com.avito.instrumentation.reservation.request.Device.CloudEmulator
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.instrumentation.stub.reservation.client.kubernetes.StubDeploymentNameGenerator
import com.avito.instrumentation.stub.reservation.client.kubernetes.createStubInstance
import com.avito.instrumentation.stub.reservation.request.createStubInstance
import com.avito.logger.StubLoggerFactory
import com.avito.truth.assertThat
import com.google.common.truth.Truth.assertThat
import io.fabric8.kubernetes.client.KubernetesClientException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.UnknownHostException

internal class KubernetesReservationClientIntegrationTest {

    private val loggerFactory = StubLoggerFactory
    private var clientOne: ReservationClient? = null
    private var clientTwo: ReservationClient? = null

    @Test
    fun `claim - throws exception - unknown host`() {
        clientOne = KubernetesReservationClient.createStubInstance(
            loggerFactory = loggerFactory,
            kubernetesUrl = "unknown-host",
            kubernetesNamespace = "emulators"
        )

        val exception = assertThrows<KubernetesClientException> {
            runBlocking {
                clientOne!!.claim(
                    reservations = listOf(
                        Reservation.Data(
                            device = CloudEmulator.createStubInstance(),
                            count = 1
                        )
                    ),
                    scope = this
                )
            }
        }

        assertThat<KubernetesClientException>(exception) {
            assertThat(message).contains(
                "Operation: [create]  " +
                    "for kind: [Deployment]  " +
                    "with name: [null]  " +
                    "in namespace: [emulators]  " +
                    "failed"
            )
            assertThat<UnknownHostException>(cause) {
                assertThat(message).contains("unknown-host")
            }
        }
    }

    /**
     * see MBS-8662
     */
    @Test
    fun `claim - throws exception - deployment already exists`() {
        // to generate single name for two different clients
        val deploymentNameGenerator = StubDeploymentNameGenerator()

        clientOne = KubernetesReservationClient.createStubInstance(
            loggerFactory = loggerFactory,
            deploymentNameGenerator = deploymentNameGenerator
        )
        clientTwo = KubernetesReservationClient.createStubInstance(
            loggerFactory = loggerFactory,
            deploymentNameGenerator = deploymentNameGenerator
        )

        val delayEnoughToCreateFirstDeploymentMs = 3000L

        val exception = assertThrows<KubernetesClientException> {
            runParallel(
                {
                    clientOne!!.claim(
                        reservations = listOf(
                            Reservation.Data(
                                device = CloudEmulator.createStubInstance(),
                                count = 1
                            )
                        ),
                        scope = this
                    )
                },
                {
                    delay(delayEnoughToCreateFirstDeploymentMs)
                    clientTwo!!.claim(
                        reservations = listOf(
                            Reservation.Data(
                                device = CloudEmulator.createStubInstance(),
                                count = 1
                            )
                        ),
                        scope = this
                    )
                }
            )
        }

        assertThat<KubernetesClientException>(exception) {
            assertThat(message).containsMatch(Regex("deployments.extensions .+ already exists").pattern)
        }
    }

    @AfterEach
    fun cleanup() {
        runParallel(
            { clientOne?.release() },
            { clientTwo?.release() }
        )
    }

    private fun runParallel(vararg runnable: suspend CoroutineScope.() -> Unit) {
        runBlocking {
            coroutineScope {
                runnable.map { async { it.invoke(this@coroutineScope) } }.awaitAll()
            }
        }
    }
}

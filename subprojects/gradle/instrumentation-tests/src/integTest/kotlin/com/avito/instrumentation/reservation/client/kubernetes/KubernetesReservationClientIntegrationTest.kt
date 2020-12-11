package com.avito.instrumentation.reservation.client.kubernetes

import com.avito.instrumentation.reservation.adb.AndroidDebugBridge
import com.avito.instrumentation.reservation.adb.EmulatorsLogsReporter
import com.avito.instrumentation.reservation.client.ReservationClient
import com.avito.instrumentation.reservation.request.Device.CloudEmulator
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.instrumentation.reservation.request.createStubInstance
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.truth.assertThat
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.createKubernetesClient
import com.avito.utils.logging.FakeCILogger
import com.google.common.truth.Truth.assertThat
import io.fabric8.kubernetes.client.KubernetesClientException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.net.UnknownHostException

internal class KubernetesReservationClientIntegrationTest {

    private val logger = FakeCILogger()
    private lateinit var client: ReservationClient

    @Test
    fun `claim - throws exception - unknown host`() {
        client = createClient(
            kubernetesUrl = "unknown-host",
            kubernetesNamespace = "emulators"
        )

        val exception = assertThrows<KubernetesClientException> {
            runBlocking {
                client.claim(
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

    @AfterEach
    fun cleanup() {
        runBlocking {
            client.release()
        }
    }

    private fun createClient(
        kubernetesUrl: String = requireNotNull(System.getProperty("avito.kubernetes.url")),
        kubernetesNamespace: String = requireNotNull(System.getProperty("avito.kubernetes.namespace")),
        configurationName: String = "integration-test",
        projectName: String = "",
        buildId: String = "19723577",
        buildType: String = "",
        registry: String = ""
    ): KubernetesReservationClient {
        val kubernetesCredentials = KubernetesCredentials.Service(
            token = requireNotNull(System.getProperty("avito.kubernetes.token")),
            caCertData = requireNotNull(System.getProperty("avito.kubernetes.cert")),
            url = kubernetesUrl
        )

        val outputFolder = File("integration")
        val logcatFolder = File("logcat")

        return KubernetesReservationClient(
            androidDebugBridge = AndroidDebugBridge(
                adb = Adb(),
                logger = { logger.info(it) }
            ),
            kubernetesClient = createKubernetesClient(
                kubernetesCredentials = kubernetesCredentials,
                namespace = kubernetesNamespace
            ),
            emulatorsLogsReporter = EmulatorsLogsReporter(
                outputFolder = outputFolder,
                logcatDir = logcatFolder,
                logcatTags = emptyList()
            ),
            logger = logger,
            reservationDeploymentFactory = ReservationDeploymentFactory(
                configurationName = configurationName,
                projectName = projectName,
                buildId = buildId,
                buildType = buildType,
                registry = registry,
                logger = logger
            )
        )
    }
}

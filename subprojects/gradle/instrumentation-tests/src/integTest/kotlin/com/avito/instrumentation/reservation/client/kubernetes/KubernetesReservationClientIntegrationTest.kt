package com.avito.instrumentation.reservation.client.kubernetes

import com.avito.instrumentation.reservation.adb.AndroidDebugBridge
import com.avito.instrumentation.reservation.adb.EmulatorsLogsReporter
import com.avito.instrumentation.reservation.request.Device.CloudEmulator
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.createKubernetesClient
import com.avito.utils.logging.FakeCILogger
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File

internal class KubernetesReservationClientIntegrationTest {

    private val client = createClient()

    @Test
    fun test() {
        runBlocking {
            client.claim(
                reservations = listOf(
                    Reservation.Data(
                        device = CloudEmulator(
                            name = "api29",
                            api = 29,
                            model = "Android_SDK_built_for_x86_64",
                            image = "avitotech/android-emulator-29:915c1f20be",
                            cpuCoresRequest = "1",
                            cpuCoresLimit = "1.3"
                        ),
                        count = 1
                    )
                ),
                scope = this
            )
        }
    }

    @AfterEach
    fun cleanup() {
        runBlocking {
            client.release()
        }
    }

    private fun createClient(): KubernetesReservationClient {
        val kubernetesCredentials = KubernetesCredentials.Service(
            token = requireNotNull(System.getProperty("avito.kubernetes.token")),
            caCertData = requireNotNull(System.getProperty("avito.kubernetes.cert")),
            url = requireNotNull(System.getProperty("avito.kubernetes.url"))
        )
        val namespace = requireNotNull(System.getProperty("avito.kubernetes.namespace"))

        val logger = FakeCILogger()
        val outputFolder = File("integration")
        val logcatFolder = File("logcat")
        val configurationName = "integration-test"
        val projectName = ""
        val buildId = "19723577"
        val buildType = ""
        val registry = ""

        return KubernetesReservationClient(
            androidDebugBridge = AndroidDebugBridge(
                adb = Adb(),
                logger = { logger.info(it) }
            ),
            kubernetesClient = createKubernetesClient(
                kubernetesCredentials = kubernetesCredentials,
                namespace = namespace
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

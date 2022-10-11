package com.avito.android.device.internal

import com.avito.android.device.AndroidApplication
import com.avito.android.device.DeviceSerial
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.request.pkg.InstallRemotePackageRequest
import com.malinskiy.adam.request.sync.v1.PushFileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import java.util.logging.Logger
import kotlin.math.roundToInt

internal class InstallPackage(
    private val adb: AndroidDebugBridgeClient,
) {

    private val logger = Logger.getLogger("InstallPackage")

    suspend fun installTo(
        application: AndroidApplication,
        serial: DeviceSerial
    ) {
        withContext(Dispatchers.IO) {
            coroutineScope {
                val absoluteRemoteFilePath = "/data/local/tmp/${application.apk.toPath().fileName}"
                val progress = adb.execute(
                    PushFileRequest(
                        application.apk,
                        absoluteRemoteFilePath,
                    ),
                    this,
                    serial.value
                )
                val (output, exitCode) = progress.receiveAsFlow()
                    .filter { currentProgress ->
                        logger.finest("Push apk $application progress $currentProgress")
                        (currentProgress * 100).roundToInt() == 100
                    }
                    .map {
                        logger.info("Start install $application")
                        adb.execute(
                            InstallRemotePackageRequest(
                                absoluteRemoteFilePath = absoluteRemoteFilePath,
                                reinstall = false,
                                extraArgs = emptyList()
                            ),
                            serial = serial.value
                        )
                    }.first()
                progress.cancel()
                if (exitCode != 0) {
                    throw RuntimeException(
                        "Install of the $application not success; \n $output"
                    )
                }
            }
        }
    }
}

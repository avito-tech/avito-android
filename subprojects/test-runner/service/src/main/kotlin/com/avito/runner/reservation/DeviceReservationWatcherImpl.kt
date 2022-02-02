package com.avito.runner.reservation

import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

internal class DeviceReservationWatcherImpl(
    private val reservation: DeviceReservation
) : DeviceReservationWatcher {

    override suspend fun watch(deviceSignals: ReceiveChannel<Device.Signal>) {
        val deployments: MutableMap<String, String> = mutableMapOf()

        with(CoroutineScope(coroutineContext)) {
            launch(Dispatchers.Default) {
                for (signal in deviceSignals) {
                    when (signal) {
                        is Device.Signal.Died -> reservation.releaseDevice(signal.coordinate)
                        is Device.Signal.ReservationNotNeeded -> {
                            deployments[signal.deviceName]?.let { deploymentName ->
                                reservation.releaseReservation(deploymentName)
                                deployments.remove(signal.deviceName)
                            }
                        }
                        is Device.Signal.NewDeployment -> {
                            deployments[signal.deviceName] = signal.deploymentName
                        }
                    }
                }
            }
        }
    }
}

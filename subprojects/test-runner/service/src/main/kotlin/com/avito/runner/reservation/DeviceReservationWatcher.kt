package com.avito.runner.reservation

import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

interface DeviceReservationWatcher {

    suspend fun watch(deviceSignals: ReceiveChannel<Device.Signal>)

    class Impl(
        private val reservation: DeviceReservation
    ) : DeviceReservationWatcher {

        override suspend fun watch(deviceSignals: ReceiveChannel<Device.Signal>) {
            with(CoroutineScope(coroutineContext)) {
                launch(Dispatchers.Default) {
                    for (signal in deviceSignals) {
                        when (signal) {
                            is Device.Signal.Died -> reservation.releaseDevice(signal.coordinate)
                        }
                    }
                }
            }
        }
    }
}

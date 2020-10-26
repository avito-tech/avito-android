package com.avito.runner.reservation

import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

interface DeviceReservationWatcher {
    fun watch(deviceSignals: ReceiveChannel<Device.Signal>, scope: CoroutineScope)

    class Impl(
        private val reservation: DeviceReservation
    ) : DeviceReservationWatcher {

        override fun watch(deviceSignals: ReceiveChannel<Device.Signal>, scope: CoroutineScope) {
            scope.launch(Dispatchers.Default) {
                for (signal in deviceSignals) {
                    when (signal) {
                        is Device.Signal.Died -> {
                            reservation.releaseDevice(signal.coordinate, scope)
                        }
                    }
                }
            }
        }
    }
}

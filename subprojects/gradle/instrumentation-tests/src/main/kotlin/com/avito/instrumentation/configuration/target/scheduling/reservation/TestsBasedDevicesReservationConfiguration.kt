package com.avito.instrumentation.configuration.target.scheduling.reservation

import com.avito.instrumentation.reservation.request.Device

open class TestsBasedDevicesReservationConfiguration : DeviceReservationConfiguration() {

    var maximum: Int? = null
    var minimum: Int = 1
    var testsPerEmulator: Int? = null

    override fun validate() {
        super.validate()

        requireNotNull(maximum)
        requireNotNull(testsPerEmulator)
    }

    companion object {
        fun create(
            device: Device,
            min: Int,
            max: Int,
            testsPerEmulator: Int = 12
        ): TestsBasedDevicesReservationConfiguration {
            return TestsBasedDevicesReservationConfiguration().apply {
                this.device = device
                maximum = max
                minimum = min
                this.testsPerEmulator = testsPerEmulator
            }
        }
    }
}

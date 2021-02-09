package com.avito.instrumentation.configuration.target.scheduling.reservation

import com.avito.instrumentation.reservation.request.Device

public open class TestsBasedDevicesReservationConfiguration : DeviceReservationConfiguration() {

    public var maximum: Int? = null
    public var minimum: Int = 1
    public var testsPerEmulator: Int? = null

    override fun validate() {
        super.validate()

        requireNotNull(maximum)
        requireNotNull(testsPerEmulator)
    }

    public companion object {

        public fun create(
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

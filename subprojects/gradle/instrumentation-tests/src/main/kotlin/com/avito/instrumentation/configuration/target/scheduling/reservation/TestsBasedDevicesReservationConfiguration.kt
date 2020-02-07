package com.avito.instrumentation.configuration.target.scheduling.reservation

open class TestsBasedDevicesReservationConfiguration : DeviceReservationConfiguration() {

    var maximum: Int? = null
    var minimum: Int = 1
    var testsPerEmulator: Int? = null

    override fun validate() {
        super.validate()

        require(maximum != null)
        require(testsPerEmulator != null)
    }
}

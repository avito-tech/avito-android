package com.avito.instrumentation.configuration.target.scheduling.reservation

open class StaticDeviceReservationConfiguration : DeviceReservationConfiguration() {

    var count: Int = 0

    override fun validate() {
        super.validate()

        require(count > 0) { "Static device reservation quota must be > 0" }
    }
}

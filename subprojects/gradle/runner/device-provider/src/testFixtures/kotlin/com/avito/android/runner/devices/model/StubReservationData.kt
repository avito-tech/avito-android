package com.avito.android.runner.devices.model

import com.avito.instrumentation.reservation.request.Device

internal fun ReservationData.Companion.stub(): ReservationData =
    ReservationData(
        device = Device.MockEmulator("stub", "", 0),
        count = 0
    )

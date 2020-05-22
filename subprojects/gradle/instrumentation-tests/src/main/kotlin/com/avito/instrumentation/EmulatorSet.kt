package com.avito.instrumentation

import com.avito.instrumentation.reservation.request.Device

object EmulatorSet {
    val fast = setOf(
        Device.Emulator.Emulator22,
        Device.Emulator.Emulator28
    )
    val full = setOf(
        Device.Emulator.Emulator22,
        Device.Emulator.Emulator23,
        Device.Emulator.Emulator24,
        Device.Emulator.Emulator28
    )
}
package com.avito.android.device.manager.internal

import com.avito.android.device.DeviceSerial
import com.malinskiy.adam.request.shell.v1.ShellCommandRequest

internal class StopEmulatorRequest(
    serial: DeviceSerial
) : ShellCommandRequest("-s ${serial.value} emu kill")

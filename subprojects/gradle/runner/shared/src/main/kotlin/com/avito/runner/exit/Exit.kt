package com.avito.runner.exit

sealed class Exit(val code: Int, val message: String?) {
    object ThereWereFailedTests : Exit(code = 1, message = "There were failed tests.")
    object NoTests : Exit(code = 1, message = "0 tests were run.")

    class DeviceIntentionSerialNotFoundInConnectedDevices(serial: String) : Exit(
        code = 1,
        message = "Device with serial: $serial not found in connected devices"
    )
}

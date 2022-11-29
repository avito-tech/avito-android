package com.avito.android.device.manager.internal.storage

import java.util.concurrent.Future

internal class AndroidDeviceProcess(
    private val runningDeviceFuture: Future<*>,
) {

    fun awaitTermination() {
        runningDeviceFuture.get()
    }
}

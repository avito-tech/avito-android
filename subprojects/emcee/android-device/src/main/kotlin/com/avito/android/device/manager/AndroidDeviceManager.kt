package com.avito.android.device.manager

import com.avito.android.device.AndroidDevice

public interface AndroidDeviceManager {
    /**
     * TODO change [type] to enum or something clarified
     */
    public suspend fun findOrStart(sdk: Int, type: String): AndroidDevice
    public suspend fun stop(device: AndroidDevice)
}

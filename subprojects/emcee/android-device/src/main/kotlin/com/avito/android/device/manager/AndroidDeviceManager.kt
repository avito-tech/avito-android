package com.avito.android.device.manager

import com.avito.android.device.AndroidDevice
import com.avito.android.device.avd.internal.AvdConfigurationProvider
import com.avito.android.device.avd.internal.StartAvd
import com.avito.android.device.manager.internal.AndroidDeviceManagerImpl
import com.avito.android.device.manager.internal.FindAndroidDevice
import com.avito.android.device.manager.internal.StartAndroidDevice
import com.avito.android.device.manager.internal.StopAndroidDevice
import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.nio.file.Path

public interface AndroidDeviceManager {
    /**
     * TODO change [type] to enum or something clarified
     */
    public suspend fun findOrStart(sdk: Int, type: String): AndroidDevice
    public suspend fun stop(device: AndroidDevice)

    public companion object {
        @ExperimentalCoroutinesApi
        public fun create(
            configurationProvider: AvdConfigurationProvider,
            androidSdk: Path
        ): AndroidDeviceManager {
            val adb = AndroidDebugBridgeClientFactory().build()
            val stopAndroidDevice = StopAndroidDevice(adb)
            val maximumRunningDevices = 1
            return AndroidDeviceManagerImpl(
                findAndroidDevice = FindAndroidDevice(adb, stopAndroidDevice, maximumRunningDevices),
                stopAndroidDevice = stopAndroidDevice,
                startAndroidDevice = StartAndroidDevice(
                    adb,
                    StartAvd(configurationProvider, androidSdk),
                    maximumRunningDevices
                )
            )
        }
    }
}

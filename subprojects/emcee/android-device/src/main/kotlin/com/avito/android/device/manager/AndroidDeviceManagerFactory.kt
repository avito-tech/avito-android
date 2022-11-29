package com.avito.android.device.manager

import com.avito.android.device.avd.internal.AvdConfigurationProvider
import com.avito.android.device.avd.internal.command.StartAvdCommand
import com.avito.android.device.avd.internal.command.StopAvdCommand
import com.avito.android.device.manager.internal.AndroidDeviceManagerImpl
import com.avito.android.device.manager.internal.FindAndroidDevice
import com.avito.android.device.manager.internal.StartAndroidDevice
import com.avito.android.device.manager.internal.StopAndroidDevice
import com.avito.android.device.manager.internal.storage.AndroidDeviceProcessStorage
import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
public object AndroidDeviceManagerFactory {

    public fun create(
        configurationProvider: AvdConfigurationProvider,
        androidSdk: Path,
        maximumRunningDevices: Int,
        executorService: ExecutorService = Executors.newSingleThreadExecutor()
    ): AndroidDeviceManager {
        val adb = AndroidDebugBridgeClientFactory().build()
        val startCommand = StartAvdCommand(configurationProvider, androidSdk)
        val stopCommand = StopAvdCommand(androidSdk)
        val storage = AndroidDeviceProcessStorage()
        val stopAndroidDevice = StopAndroidDevice(stopCommand, storage)
        return AndroidDeviceManagerImpl(
            findAndroidDevice = FindAndroidDevice(adb, maximumRunningDevices),
            stopAndroidDevice = stopAndroidDevice,
            startAndroidDevice = StartAndroidDevice(adb, startCommand, executorService, storage)
        )
    }
}

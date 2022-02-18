package com.avito.emcee.worker.internal

import com.avito.emcee.device.AndroidApplication
import com.avito.emcee.device.AndroidDevice

internal sealed class AndroidDeviceState {

    protected abstract val device: AndroidDevice

    protected suspend fun install(apps: List<AndroidApplication>) =
        apps.fold(listOf<AndroidApplication>()) { installedApps, app ->
            device.install(app).onFailure { throw it }
            installedApps + app
        }

    abstract suspend fun prepareStateForExecution(newApps: List<AndroidApplication>): AndroidDeviceState
    abstract suspend fun clean(): AndroidDeviceState
    abstract fun testExecuted(): AndroidDeviceState

    class Clean(override val device: AndroidDevice) : AndroidDeviceState() {

        override suspend fun prepareStateForExecution(newApps: List<AndroidApplication>): AndroidDeviceState {
            return InstalledApplications(device, install(newApps))
        }

        override suspend fun clean(): AndroidDeviceState {
            // do nothing
            return this
        }

        override fun testExecuted(): AndroidDeviceState {
            throw UnsupportedOperationException(
                "Can't change state. Because test can't be executed without installed apps"
            )
        }
    }

    open class InstalledApplications(
        override val device: AndroidDevice,
        val apps: List<AndroidApplication>,
    ) : AndroidDeviceState() {

        override suspend fun prepareStateForExecution(newApps: List<AndroidApplication>): AndroidDeviceState {
            require(apps == newApps) {
                "Can't prepare state. Installed apps:$apps are not equal to newApps:$newApps"
            }
            return this
        }

        override suspend fun clean(): AndroidDeviceState {
            apps.forEach { app ->
                device.uninstall(app).onFailure { throw it }
            }
            return Clean(device)
        }

        override fun testExecuted(): AndroidDeviceState {
            return WithDataOnDisk(device, apps)
        }
    }

    class WithDataOnDisk(
        device: AndroidDevice,
        apps: List<AndroidApplication>
    ) : AndroidDeviceState.InstalledApplications(device, apps) {

        override suspend fun prepareStateForExecution(newApps: List<AndroidApplication>): AndroidDeviceState {
            require(apps == newApps) {
                "Can't prepare state. Installed apps:$apps are not equal to newApps:$newApps"
            }
            apps.forEach { app -> device.clearPackage(app.packageName).onFailure { throw it } }
            return InstalledApplications(device, apps)
        }
    }
}

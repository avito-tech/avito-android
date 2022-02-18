package com.avito.emcee.device.internal

import com.avito.emcee.device.AndroidApplication
import com.avito.emcee.device.AndroidDevice

internal class AndroidDeviceImpl : AndroidDevice {

    override suspend fun install(application: AndroidApplication): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun isAlive(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun executeInstrumentation(args: List<String>): Result<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun clearPackage(appPackage: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun uninstall(app: AndroidApplication): Result<Unit> {
        TODO("Not yet implemented")
    }
}

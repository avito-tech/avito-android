package com.avito.emcee.device

public interface AndroidDevice {
    public suspend fun install(application: AndroidApplication): Result<Unit>
    public suspend fun isAlive(): Boolean
    public suspend fun executeInstrumentation(args: List<String>): Result<Any>
    public suspend fun clearPackage(appPackage: String): Result<Unit>
    public suspend fun uninstall(app: AndroidApplication): Result<Unit>
}

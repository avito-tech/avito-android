package com.avito.runner.service

public interface DeviceWorkerPool {
    public suspend fun start()
    public suspend fun stop()
}

package com.avito.runner.scheduler.runner.scheduler

import com.avito.android.runner.devices.DevicesProviderFactory

public interface TestsSchedulerFactory {

    public fun create(devicesProviderFactory: DevicesProviderFactory): TestsScheduler
}

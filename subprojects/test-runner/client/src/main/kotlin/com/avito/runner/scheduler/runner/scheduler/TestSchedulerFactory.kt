package com.avito.runner.scheduler.runner.scheduler

import com.avito.android.runner.devices.DevicesProviderFactory

public interface TestSchedulerFactory {

    public fun create(devicesProviderFactory: DevicesProviderFactory): TestScheduler
}

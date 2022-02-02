package com.avito.emcee

import com.avito.emcee.queue.Device
import com.avito.emcee.queue.Job
import com.avito.emcee.queue.ScheduleStrategy
import com.avito.emcee.queue.TestExecutionBehavior
import com.avito.emcee.queue.TestTimeoutConfiguration
import java.io.File

public class EmceeTestActionConfig(
    public val job: Job,
    public val scheduleStrategy: ScheduleStrategy,
    public val testExecutionBehavior: TestExecutionBehavior,
    public val timeoutConfiguration: TestTimeoutConfiguration,
    public val devices: List<Device>,
    public val apk: File,
    public val testApk: File,
)

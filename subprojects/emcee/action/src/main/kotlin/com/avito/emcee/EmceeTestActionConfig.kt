package com.avito.emcee

import com.avito.emcee.internal.FilePathAdapter
import com.avito.emcee.queue.Device
import com.avito.emcee.queue.Job
import com.avito.emcee.queue.ScheduleStrategy
import com.avito.emcee.queue.TestExecutionBehavior
import com.avito.emcee.queue.TestTimeoutConfiguration
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import java.io.File

@JsonClass(generateAdapter = true)
public class EmceeTestActionConfig(
    public val job: Job,
    public val scheduleStrategy: ScheduleStrategy,
    public val testExecutionBehavior: TestExecutionBehavior,
    public val timeoutConfiguration: TestTimeoutConfiguration,
    public val devices: List<Device>,
    public val apk: File,
    public val testApk: File,
) {

    public companion object {

        @OptIn(ExperimentalStdlibApi::class)
        public fun createMoshiAdapter(): JsonAdapter<EmceeTestActionConfig> {
            val moshi = Moshi.Builder()
                .add(FilePathAdapter())
                .build()
            return moshi.adapter()
        }
    }
}

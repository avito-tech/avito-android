package com.avito.emcee.client

import com.avito.emcee.client.internal.ArtifactorySettings
import com.avito.emcee.client.internal.FilePathAdapter
import com.avito.emcee.queue.DeviceConfiguration
import com.avito.emcee.queue.Job
import com.avito.emcee.queue.ScheduleStrategy
import com.avito.emcee.queue.TestExecutionBehavior
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import java.io.File

@JsonClass(generateAdapter = true)
public class EmceeTestClientConfig(
    public val job: Job,
    public val artifactory: ArtifactorySettings,
    public val scheduleStrategy: ScheduleStrategy,
    public val testExecutionBehavior: TestExecutionBehavior,
    public val testMaximumDurationSec: Long,
    public val devices: List<DeviceConfiguration>,
    public val apk: File,
    public val appPackage: String,
    public val testApk: File,
    public val testAppPackage: String,
    public val testRunnerClass: String,
) {

    public companion object {

        @OptIn(ExperimentalStdlibApi::class)
        public fun createMoshiAdapter(): JsonAdapter<EmceeTestClientConfig> {
            val moshi = Moshi.Builder()
                .add(FilePathAdapter())
                .build()
            return moshi.adapter()
        }
    }
}

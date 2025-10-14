package com.avito.android.plugin.build_metrics.internal.gradle.app_build.clickstream

import com.avito.android.clickstream.event.ClickStreamEvent
import com.avito.android.plugin.build_metrics.BuildEnvironment
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.AppBuildTimeMetadata
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.ApplicationType

internal class AppBuildTimeMetric(
    private val duration: Long,
    private val status: String,
    private val appName: String,
    private val appType: ApplicationType,
    private val metadata: AppBuildTimeMetadata,
    private val environment: BuildEnvironment,
) : BuildMetric.ClickStream() {

    override fun asClickStream(): ClickStreamEvent {
        return AppBuildTimeClickStreamEvent(
            duration = duration,
            status = status,
            appName = appName,
            appType = appType,
            devName = metadata.userName,
            branchName = metadata.branchName,
            repoName = metadata.repoName,
            environment = environment
        )
    }
}

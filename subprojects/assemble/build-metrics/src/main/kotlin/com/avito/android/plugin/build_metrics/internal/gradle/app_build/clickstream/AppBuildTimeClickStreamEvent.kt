package com.avito.android.plugin.build_metrics.internal.gradle.app_build.clickstream

import com.avito.android.clickstream.ClickStreamSrcId
import com.avito.android.clickstream.event.ClickStreamEvent
import com.avito.android.clickstream.event.ParametrizedClickStreamEvent
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.ApplicationType

public data class AppBuildTimeClickStreamEvent(
    private val duration: Long,
    private val status: String,
    private val appName: String,
    private val appType: ApplicationType,
    private val devName: String,
) : ClickStreamEvent by ParametrizedClickStreamEvent(
    eventId = 16489,
    version = 1,
    params = mapOf(
        "android_build_duration" to duration,
        "android_build_status" to status,
        "android_app_type" to appType.code,
        "app_package_name" to appName,
        "dev_name" to devName,
    ),
    srcId = ClickStreamSrcId.ANDROID_DEMO_APPS,
)

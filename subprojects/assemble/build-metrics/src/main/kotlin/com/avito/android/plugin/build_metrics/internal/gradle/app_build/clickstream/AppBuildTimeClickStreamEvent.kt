package com.avito.android.plugin.build_metrics.internal.gradle.app_build.clickstream

import com.avito.android.clickstream.ClickStreamSrcId
import com.avito.android.clickstream.event.ClickStreamEvent
import com.avito.android.clickstream.event.ParametrizedClickStreamEvent
import com.avito.android.plugin.build_metrics.BuildEnvironment
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.ApplicationType
import java.time.Clock

public data class AppBuildTimeClickStreamEvent(
    private val duration: Long,
    private val status: String,
    private val appName: String,
    private val appType: ApplicationType,
    private val devName: String,
    private val branchName: String,
    private val repoName: String,
    private val environment: BuildEnvironment,
) : ClickStreamEvent by ParametrizedClickStreamEvent(
    eventId = 16489,
    version = 2,
    params = mapOf(
        "android_build_duration" to duration,
        "android_build_status" to status,
        "android_app_type" to appType.code,
        "app_package_name" to appName,
        "dev_name" to devName,
        "ldap_user" to devName,
        "jira_issue" to extractJiraIssue(branchName).orEmpty(),
        "repo_name" to repoName,
        "timestamp" to Clock.systemDefaultZone().millis(),
        "dev_tool_name" to "gradle",
        "dev_tool_env" to environment.code,
    ),
    srcId = ClickStreamSrcId.ANDROID_DEMO_APPS,
)

private fun extractJiraIssue(branchName: String): String? {
    val jiraIssue = Regex("([A-Z]+-[0-9]+)").find(branchName)
    return jiraIssue?.groupValues?.get(0)
}

package com.avito.teamcity.metric

import com.avito.android.clickstream.ClickStreamSrcId
import com.avito.android.clickstream.event.ClickStreamEvent
import com.avito.android.clickstream.event.ParametrizedClickStreamEvent
import com.avito.teamcity.saturate.bitbucket.pullRequest
import org.jetbrains.teamcity.rest.Build
import java.time.Duration

internal class TeamcityBuildOverallDurationMetric(
    private val build: Build
) {

    fun asClickstreamEvent(): ClickStreamEvent {
        val queuedDateTime = requireNotNull(build.queuedDateTime) {
            "queuedDateTime can't be null for finished builds"
        }
        return TeamcityBuildDurationEvent(
            repoName = build.repoName.orEmpty(),
            jiraIssue = build.jiraIssue.orEmpty(),
            ldapUser = build.ldapUser.orEmpty(),
            timestamp = queuedDateTime.toInstant().epochSecond,
            duration = Duration.between(queuedDateTime, build.finishDateTime).seconds,
            status = build.status?.name?.lowercase().orEmpty(),
            toolName = "gradle",
            environment = "ci",
            configurationId = build.buildConfigurationId.stringId,
        )
    }

    data class TeamcityBuildDurationEvent(
        val repoName: String,
        val jiraIssue: String,
        val ldapUser: String,
        val timestamp: Long,
        val duration: Long,
        val status: String,
        val toolName: String,
        val environment: String,
        val configurationId: String,
    ) : ClickStreamEvent by ParametrizedClickStreamEvent(
        eventId = 17306,
        version = 1,
        srcId = ClickStreamSrcId.SDLC,
        params = mapOf(
            "jira_issue" to jiraIssue,
            "repo_name" to repoName.lowercase(),
            "ldap_user" to ldapUser,
            "timestamp" to timestamp,
            "event_duration" to duration,
            "status" to status,
            "dev_tool_name" to toolName,
            "dev_tool_env" to environment,
            "configuration_id" to configurationId,
        )
    )
}

private val Build.repoName: String?
    get() {
        val key = parameters.find { it.name == "env.BITBUCKET_PROJECT" }?.value ?: return null
        val name = parameters.find { it.name == "env.BITBUCKET_REPOSITORY" }?.value ?: return null
        return "$key/$name"
    }

private val JIRA_ISSUE_REGEXP = "^([A-Z]+-[0-9]+)".toRegex()

private val Build.jiraIssue: String?
    get() = branch.name?.let { JIRA_ISSUE_REGEXP.find(it)?.groupValues?.get(0) }

private val Build.ldapUser: String?
    get() = pullRequest()?.author?.user?.slug
            ?: triggeredInfo?.user?.username
            ?: changes.lastOrNull()?.user?.username

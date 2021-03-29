package com.avito.test.summary

import com.avito.report.model.Team
import com.avito.slack.model.SlackChannelId
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class TestSummaryExtension @Inject constructor(objects: ObjectFactory) {

    val slackToken = objects.property<String>()

    val slackWorkspace = objects.property<String>()

    val reportsHost = objects.property<String>()

    val summaryChannel = objects.property<SlackChannelId>()

    val buildUrl = objects.property<String>()

    val currentBranch = objects.property<String>()

    val reportViewerUrl = objects.property<String>()

    val unitToChannelMapping = objects.mapProperty<Team, SlackChannelId>()

    val mentionOnFailures = objects.setProperty(String::class.java)

    val reserveSlackChannel = objects.property<SlackChannelId>()

    val slackUserName = objects.property<String>()
}

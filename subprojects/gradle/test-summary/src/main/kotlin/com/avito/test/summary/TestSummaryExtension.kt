package com.avito.test.summary

import com.avito.slack.model.SlackChannel
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class TestSummaryExtension @Inject constructor(objects: ObjectFactory) {

    val slackToken = objects.property<String>()

    val slackWorkspace = objects.property<String>()

    val reportsHost = objects.property<String>()

    // todo was "#android-test-summary"
    val summaryChannel = objects.property<SlackChannel>()

    val buildUrl = objects.property<String>()

    val currentBranch = objects.property<String>()

    val reportViewerUrl = objects.property<String>()

    val unitToChannelMapping = objects.mapProperty<String, String>()

    // todo was setOf(Team("buyer-x"))
    val mentionOnFailures = objects.setProperty(String::class.java)

    // todo was "#speed-testing-team"
    val reserveSlackChannel = objects.property<SlackChannel>()

    // todo was "Test Analyzer"
    val slackUserName = objects.property<String>()
}

package com.avito.test.summary

import com.avito.report.model.Team
import com.avito.slack.model.SlackChannel
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

public abstract class TestSummaryExtension {

    public abstract val slackToken: Property<String>

    public abstract val slackWorkspace: Property<String>

    public abstract val reportsHost: Property<String>

    public abstract val summaryChannel: Property<SlackChannel>

    public abstract val buildUrl: Property<String>

    public abstract val currentBranch: Property<String>

    public abstract val reportViewerUrl: Property<String>

    public abstract val unitToChannelMapping: MapProperty<Team, SlackChannel>

    public abstract val mentionOnFailures: SetProperty<String>

    public abstract val reserveSlackChannel: Property<SlackChannel>

    public abstract val slackUserName: Property<String>
}

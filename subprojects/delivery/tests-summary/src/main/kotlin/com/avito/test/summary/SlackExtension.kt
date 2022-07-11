package com.avito.test.summary

import com.avito.report.model.Team
import com.avito.slack.model.SlackChannel
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input

public abstract class SlackExtension {

    @get:Input
    public abstract val token: Property<String>

    @get:Input
    public abstract val workspace: Property<String>

    @get:Input
    public abstract val username: Property<String>

    @get:Input
    public abstract val unitToChannelMapping: MapProperty<Team, SlackChannel>

    @get:Input
    public abstract val summaryChannel: Property<SlackChannel>

    @get:Input
    public abstract val reserveChannel: Property<SlackChannel>

    @get:Input
    public abstract val mentionOnFailures: SetProperty<String>
}

package com.avito.test.summary

import com.avito.alertino.model.AlertinoRecipient
import com.avito.report.model.Team
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input

public abstract class AlertinoExtension {

    @get:Input
    public abstract val alertinoEndpoint: Property<String>

    @get:Input
    public abstract val alertinoTemplate: Property<String> // should be '{{text}}'

    @get:Input
    public abstract val alertinoTemplatePlaceholder: Property<String>

    @get:Input
    public abstract val unitToChannelMapping: MapProperty<Team, AlertinoRecipient>

    @get:Input
    public abstract val summaryChannel: Property<AlertinoRecipient>

    @get:Input
    public abstract val reserveChannel: Property<AlertinoRecipient>

    @get:Input
    public abstract val mentionOnFailures: SetProperty<String>
}

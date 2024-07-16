package com.avito.test.summary

import com.avito.alertino.model.AlertinoRecipient
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

public abstract class AlertinoExtension {

    @get:Input
    public abstract val alertinoEndpoint: Property<String>

    @get:Input
    public abstract val alertinoTemplate: Property<String> // should be '{{text}}'

    @get:Input
    public abstract val alertinoTemplatePlaceholder: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val testSummaryDestination: RegularFileProperty

    @get:Input
    public abstract val summaryChannel: Property<AlertinoRecipient>

    @get:Input
    public abstract val reserveChannel: Property<AlertinoRecipient>
}

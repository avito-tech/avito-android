package com.avito.instrumentation.internal.test

import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration.Data
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.ObjectOutputStream
import javax.inject.Inject

@Suppress("UnstableApiUsage")
public abstract class DumpConfigurationTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    @Input
    public val configuration: Property<Data> = objects.property()

    @OutputFile
    public val output: RegularFileProperty = objects.fileProperty()

    @TaskAction
    public fun action() {
        ObjectOutputStream(
            output.asFile.get().outputStream()
        ).use {
            it.writeObject(configuration.get())
        }
    }
}

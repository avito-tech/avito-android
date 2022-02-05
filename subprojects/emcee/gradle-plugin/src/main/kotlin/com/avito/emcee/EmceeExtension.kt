package com.avito.emcee

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import java.time.Duration

public interface EmceeExtension {

    @get:Nested
    public val job: JobConfiguration

    public val retries: Property<Int>

    public val deviceApis: ListProperty<Int>

    public val testTimeout: Property<Duration>

    public val queueBaseUrl: Property<String>

    public val configTestMode: Property<Boolean>

    public val outputDir: DirectoryProperty
}

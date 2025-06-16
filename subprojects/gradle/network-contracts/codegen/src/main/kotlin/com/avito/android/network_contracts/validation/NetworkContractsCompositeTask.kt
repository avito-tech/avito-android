package com.avito.android.network_contracts.validation

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles

public abstract class NetworkContractsCompositeTask : DefaultTask() {

    @get:InputFiles
    public abstract val reports: ConfigurableFileCollection

    @get:OutputFiles
    public val output: ConfigurableFileCollection get() = reports
}

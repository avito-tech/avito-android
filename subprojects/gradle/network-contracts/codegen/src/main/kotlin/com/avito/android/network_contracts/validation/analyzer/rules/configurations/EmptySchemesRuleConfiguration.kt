package com.avito.android.network_contracts.validation.analyzer.rules.configurations

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

internal interface EmptySchemesRuleConfiguration : RuleConfiguration {

    @get:Input
    val modulePath: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemes: ConfigurableFileCollection
}

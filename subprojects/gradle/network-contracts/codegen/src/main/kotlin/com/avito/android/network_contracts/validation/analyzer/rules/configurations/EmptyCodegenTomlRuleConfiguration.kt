package com.avito.android.network_contracts.validation.analyzer.rules.configurations

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

internal interface EmptyCodegenTomlRuleConfiguration : RuleConfiguration {

    @get:Optional
    @get:InputFile
    val codegenTomlFile: RegularFileProperty

    @get:Input
    val modulePath: Property<String>
}

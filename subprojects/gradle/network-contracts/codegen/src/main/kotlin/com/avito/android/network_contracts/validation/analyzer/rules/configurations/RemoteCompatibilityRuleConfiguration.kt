package com.avito.android.network_contracts.validation.analyzer.rules.configurations

import com.avito.android.network_contracts.internal.analytics.NetworkContractsAnalyticsService
import com.avito.android.network_contracts.validation.data.ValidationApiSchemesService
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

internal interface RemoteCompatibilityRuleConfiguration : RuleConfiguration {

    @get:InputFiles
    @get:Optional
    val schemes: ConfigurableFileCollection

    @get:Input
    val branchName: Property<String>

    @get:Input
    val modulePath: Property<String>

    @get:Internal
    val validationService: Property<ValidationApiSchemesService>

    @get:Internal
    val analyticsTrackerService: Property<NetworkContractsAnalyticsService>
}

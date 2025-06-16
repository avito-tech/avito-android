package com.avito.android.network_contracts.validation.analyzer.rules.configurations

import org.gradle.api.Named
import org.gradle.api.tasks.Input

public interface RuleConfiguration : Named {

    @Input
    override fun getName(): String
}

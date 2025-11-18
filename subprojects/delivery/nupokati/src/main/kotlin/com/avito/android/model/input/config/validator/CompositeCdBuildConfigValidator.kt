package com.avito.android.model.input.config.validator

import com.avito.android.model.input.config.CdBuildConfig

internal class CompositeCdBuildConfigValidator<in CDBuildConfig : CdBuildConfig>(
    private val validators: List<CdBuildConfigValidator<CDBuildConfig>>,
) : CdBuildConfigValidator<CDBuildConfig> {
    override fun validate(config: CDBuildConfig) {
        validators.forEach { it.validate(config) }
    }
}

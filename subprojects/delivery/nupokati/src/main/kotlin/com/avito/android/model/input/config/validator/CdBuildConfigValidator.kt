package com.avito.android.model.input.config.validator

import com.avito.android.model.input.config.CdBuildConfig

internal interface CdBuildConfigValidator<in CDBuildConfig : CdBuildConfig> {
    fun validate(config: CDBuildConfig)
}

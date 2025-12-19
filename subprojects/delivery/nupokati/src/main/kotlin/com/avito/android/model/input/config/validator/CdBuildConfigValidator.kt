package com.avito.android.model.input.config.validator

import com.avito.android.model.input.config.CdBuildConfig

public interface CdBuildConfigValidator<in CDBuildConfig : CdBuildConfig> {
    public fun validate(config: CDBuildConfig)
}

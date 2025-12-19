package com.avito.android.model.input.v2

import com.avito.android.model.input.config.CdBuildConfig
import com.avito.android.model.input.config.validator.CdBuildConfigValidator

internal class StubCdBuildConfigValidator<T : CdBuildConfig> : CdBuildConfigValidator<T> {

    override fun validate(config: T) {
    }
}

package com.avito.android.model.input.v3.validator

import com.avito.android.model.input.CdBuildConfigV3

internal interface CdBuildConfigValidator {

    fun validate(config: CdBuildConfigV3)
}

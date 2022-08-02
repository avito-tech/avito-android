package com.avito.android.model.input.v3.validator

import com.avito.android.model.input.CdBuildConfigV3

internal class CompositeCdBuildConfigValidator(
    private val validators: List<CdBuildConfigValidator>
) : CdBuildConfigValidator {

    override fun validate(config: CdBuildConfigV3) {
        validators.forEach { it.validate(config) }
    }
}

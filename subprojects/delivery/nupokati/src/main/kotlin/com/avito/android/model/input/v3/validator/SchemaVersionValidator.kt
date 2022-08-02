package com.avito.android.model.input.v3.validator

import com.avito.android.model.input.CdBuildConfigV3

internal class SchemaVersionValidator(private val currentSchemaVersion: Long) : CdBuildConfigValidator {

    override fun validate(config: CdBuildConfigV3) {
        require(config.schemaVersion == currentSchemaVersion) {
            "Unsupported schema version: ${config.schemaVersion}. Required: $currentSchemaVersion"
        }
    }
}

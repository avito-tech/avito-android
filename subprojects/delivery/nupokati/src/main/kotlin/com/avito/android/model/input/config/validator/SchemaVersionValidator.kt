package com.avito.android.model.input.config.validator

import com.avito.android.model.input.config.CdBuildConfig

public class SchemaVersionValidator(private val currentSchemaVersion: Long) : CdBuildConfigValidator<CdBuildConfig> {

    override fun validate(config: CdBuildConfig) {
        require(config.schemaVersion == currentSchemaVersion) {
            "Unsupported schema version: ${config.schemaVersion}. Required: $currentSchemaVersion"
        }
    }
}

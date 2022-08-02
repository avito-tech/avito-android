package com.avito.android.model.input.v3.validator

internal val strictCdBuildConfigValidator: CdBuildConfigValidator = CompositeCdBuildConfigValidator(
    listOf(
        SchemaVersionValidator(currentSchemaVersion = 3L),
    )
)

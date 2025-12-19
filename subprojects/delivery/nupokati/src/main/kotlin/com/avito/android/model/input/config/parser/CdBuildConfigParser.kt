package com.avito.android.model.input.config.parser

import com.avito.android.model.input.config.CdBuildConfig
import com.avito.android.model.input.config.CdBuildConfigV2
import com.avito.android.model.input.config.CdBuildConfigV3
import com.avito.android.model.input.config.CdBuildConfigV4
import com.avito.android.model.input.config.validator.CdBuildConfigValidator
import com.avito.android.model.input.config.validator.CompositeCdBuildConfigValidator
import com.avito.android.model.input.config.validator.QappsCdBuildConfigValidator
import com.avito.android.model.input.config.validator.SchemaVersionValidator
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

public object CdBuildConfigParser {

    private val json: Json = Json { ignoreUnknownKeys = true }

    public fun parseCdBuildConfigV2(file: File): CdBuildConfigV2 = transform(
        file,
        CompositeCdBuildConfigValidator(
            listOf(
                SchemaVersionValidator(2L),
                QappsCdBuildConfigValidator()
            )
        )
    )

    public fun parseCdBuildConfigV3(file: File): CdBuildConfigV3 = transform(
        file,
        SchemaVersionValidator(3L)
    )

    public fun parseCdBuildConfigV4(file: File): CdBuildConfigV4 = transform(
        file,
        SchemaVersionValidator(4L)
    )

    private inline fun <reified CDBuildConfig : CdBuildConfig> transform(
        configFile: File,
        validator: CdBuildConfigValidator<CDBuildConfig>,
    ): CDBuildConfig {
        require(configFile.exists()) {
            "Can't find cd config file in $configFile"
        }
        val cdBuildConfig = json.decodeFromStream<CDBuildConfig>(configFile.inputStream())
        validator.validate(cdBuildConfig)
        return cdBuildConfig
    }
}

package com.avito.android.model.input.config.parser

import com.avito.android.model.input.config.CdBuildConfig
import com.avito.android.model.input.config.validator.CdBuildConfigValidator
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.file.RegularFile
import java.io.File

internal class CdBuildConfigParser<CDBuildConfig : CdBuildConfig>(
    private val validator: CdBuildConfigValidator<CDBuildConfig>
) {

    val json = Json { ignoreUnknownKeys = true }

    inline fun <reified T : CDBuildConfig> transform(configFilePath: RegularFile): T {
        val configFile = configFilePath.asFile
        val cdBuildConfig = deserializeToCdBuildConfig<T>(configFile)
        validator.validate(cdBuildConfig)
        return cdBuildConfig
    }

    private inline fun <reified B : CDBuildConfig> deserializeToCdBuildConfig(configFile: File): B {
        require(configFile.exists()) {
            "Can't find cd config file in $configFile"
        }
        return json.decodeFromStream<B>(configFile.inputStream())
    }
}

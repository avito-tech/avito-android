package com.avito.android.model.input.v3

import com.avito.android.model.input.CdBuildConfigV3
import com.avito.android.model.input.v3.validator.CdBuildConfigValidator
import com.avito.android.model.input.v3.validator.strictCdBuildConfigValidator
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.Transformer
import org.gradle.api.file.RegularFile
import java.io.File

internal class CdBuildConfigParser(
    private val validator: CdBuildConfigValidator = strictCdBuildConfigValidator
) : Transformer<CdBuildConfigV3, RegularFile> {

    override fun transform(configFilePath: RegularFile): CdBuildConfigV3 {
        val configFile = configFilePath.asFile
        val cdBuildConfig = deserializeToCdBuildConfig(configFile)
        validator.validate(cdBuildConfig)
        return cdBuildConfig
    }

    private fun deserializeToCdBuildConfig(configFile: File): CdBuildConfigV3 {
        require(configFile.exists()) {
            "Can't find cd config file in $configFile"
        }
        return Json.decodeFromStream(configFile.inputStream())
    }
}

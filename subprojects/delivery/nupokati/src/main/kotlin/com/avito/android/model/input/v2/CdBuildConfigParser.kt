package com.avito.android.model.input.v2

import com.avito.android.model.input.CdBuildConfigV2
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.Transformer
import org.gradle.api.file.RegularFile
import java.io.File

internal class CdBuildConfigParser(
    private val validator: CdBuildConfigValidator = StrictCdBuildConfigValidator()
) : Transformer<CdBuildConfigV2, RegularFile> {

    override fun transform(configFilePath: RegularFile): CdBuildConfigV2 {
        val configFile = configFilePath.asFile

        return deserializeToCdBuildConfig(configFile).also {
            validator.validate(it)
        }
    }

    private fun deserializeToCdBuildConfig(configFile: File): CdBuildConfigV2 {
        require(configFile.exists()) {
            "Can't find cd config file in $configFile"
        }
        return Json.decodeFromStream(configFile.inputStream())
    }
}

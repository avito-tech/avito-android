package com.avito.android.model.input.config.parser

import com.avito.android.model.input.config.CdBuildConfig
import com.avito.android.model.input.config.CdBuildConfigV3
import com.avito.android.model.input.config.CdBuildConfigV4
import com.avito.android.model.input.config.validator.CompositeCdBuildConfigValidator
import com.avito.android.model.input.config.validator.QappsCdBuildConfigValidator
import com.avito.android.model.input.config.validator.SchemaVersionValidator
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.Transformer
import org.gradle.api.file.RegularFile
import java.io.File

internal class CdBuildConfigParserFactory : Transformer<CdBuildConfig, RegularFile> {

    override fun transform(configFilePath: RegularFile): CdBuildConfig {
        return when (val version = parseSchemaVersion(configFilePath.asFile)) {
            2L -> CdBuildConfigParser(
                CompositeCdBuildConfigValidator(
                    listOf(
                        SchemaVersionValidator(2L),
                        QappsCdBuildConfigValidator()
                    )
                )
            ).transform(configFilePath)

            3L -> CdBuildConfigParser<CdBuildConfigV3>(
                SchemaVersionValidator(3L)
            ).transform(configFilePath)

            4L -> CdBuildConfigParser<CdBuildConfigV4>(
                SchemaVersionValidator(4L)
            ).transform(configFilePath)

            else -> throw IllegalArgumentException("Unsupported schema version: $version")
        }
    }

    private fun parseSchemaVersion(configFilePath: File): Long {
        val ignoringJson = Json { ignoreUnknownKeys = true }
        return ignoringJson.decodeFromStream<SchemaOnly>(configFilePath.inputStream()).schemaVersion
    }

    @Serializable
    private data class SchemaOnly(@SerialName("schema_version") val schemaVersion: Long)
}

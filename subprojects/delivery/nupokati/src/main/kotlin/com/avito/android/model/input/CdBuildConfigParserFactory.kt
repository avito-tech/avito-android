package com.avito.android.model.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.Transformer
import org.gradle.api.file.RegularFile
import java.io.File

internal class CdBuildConfigParserFactory : Transformer<CdBuildConfig, RegularFile> {

    override fun transform(configFilePath: RegularFile): CdBuildConfig {
        val actualParser = when (val version = parseSchemaVersion(configFilePath.asFile)) {
            2L -> com.avito.android.model.input.v2.CdBuildConfigParser()
            3L -> com.avito.android.model.input.v3.CdBuildConfigParser()
            else -> throw IllegalArgumentException("Unsupported schema version: $version")
        }

        return actualParser.transform(configFilePath)
    }

    private fun parseSchemaVersion(configFilePath: File): Long {
        val ignoringJson = Json { ignoreUnknownKeys = true }
        return ignoringJson.decodeFromStream<SchemaOnly>(configFilePath.inputStream()).schemaVersion
    }

    @Serializable
    private data class SchemaOnly(@SerialName("schema_version") val schemaVersion: Long)
}

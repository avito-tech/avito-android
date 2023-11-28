package com.avito.android.network_contracts.scheme.imports

import com.avito.android.network_contracts.scheme.imports.data.models.SchemaEntry
import java.io.File

internal class ApiSchemesFilesGenerator(
    private val rootDir: File,
) {

    fun generateFiles(schemas: List<SchemaEntry>): List<File> {
        return schemas.map(::generateSchemaFile)
    }

    private fun generateSchemaFile(schema: SchemaEntry): File {
        val file = File(rootDir, schema.path)
        file.parentFile.mkdirs()

        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText(schema.decodedContent)
        return file
    }
}

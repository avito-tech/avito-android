package com.avito.android.contracts.platform.scheme.imports

import com.avito.android.contracts.platform.scheme.imports.data.models.SchemaEntry
import java.io.File

public class ApiSchemesFilesGenerator(
    private val rootDir: File,
) {

    public fun generateFiles(schemas: List<SchemaEntry>): List<File> {
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

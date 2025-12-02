package com.avito.android.contracts.platform.scheme.imports

import com.avito.android.contracts.platform.scheme.imports.data.SchemesImportService
import com.avito.android.contracts.platform.scheme.imports.data.models.areSchemesExist
import com.avito.android.contracts.platform.scheme.imports.data.models.entriesList
import java.io.File

public class ApiSchemesImportFacade internal constructor(
    private val service: SchemesImportService<*>,
) {

    public suspend fun importSchemes(gateway: String, url: String, targetDirectory: File): List<File> {
        val schemes = service.importScheme(gateway, url).result
        val generator = ApiSchemesFilesGenerator(targetDirectory)
        if (!schemes.areSchemesExist) {
            error("Did not find any schemes for `$url`.")
        }

        return generator.generateFiles(schemes.entriesList())
    }
}

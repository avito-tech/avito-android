package com.avito.android.network_contracts.scheme.imports

import com.avito.android.network_contracts.scheme.imports.data.ApiSchemesImportService
import com.avito.android.network_contracts.scheme.imports.data.ApiSchemesImportServiceImpl
import com.avito.android.network_contracts.scheme.imports.data.models.areSchemesExist
import com.avito.android.network_contracts.scheme.imports.data.models.entriesList
import io.ktor.client.HttpClient
import java.io.File

public class ApiSchemesImportFacade internal constructor(
    private val service: ApiSchemesImportService,
) {

    public suspend fun importSchemes(url: String, targetDirectory: File): List<File> {
        val schemes = service.importScheme(url).result
        val generator = ApiSchemesFilesGenerator(targetDirectory)
        if (!schemes.areSchemesExist) {
            error("Did not find any schemes for `$url`.")
        }

        return generator.generateFiles(schemes.entriesList())
    }

    public companion object {

        public fun createInstance(httpClient: HttpClient): ApiSchemesImportFacade {
            return ApiSchemesImportFacade(
                service = ApiSchemesImportServiceImpl(httpClient),
            )
        }
    }
}

package com.avito.android.contracts.platform.scheme.imports.data

import com.avito.android.contracts.platform.scheme.imports.data.models.ApiSchemeImportResponse
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

public interface SchemesImportService<T : SchemesImportService.Parameters> : BuildService<T> {

    public suspend fun importScheme(gateway: String, url: String): ApiSchemeImportResponse

    public interface Parameters : BuildServiceParameters

    public companion object {

        public fun <T : SchemesImportService<P>, P : SchemesImportService.Parameters> provideImportService(
            project: Project,
            klass: Class<T>,
            configuration: Action<P>,
        ): Provider<T> {
            return project.gradle.sharedServices.registerIfAbsent(
                klass.name,
                klass,
            ) {
                it.parameters(configuration)
            }
        }
    }
}

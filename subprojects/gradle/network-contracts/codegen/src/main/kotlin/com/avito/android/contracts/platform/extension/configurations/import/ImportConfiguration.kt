package com.avito.android.contracts.platform.extension.configurations.import

import com.avito.android.contracts.platform.scheme.imports.data.SchemesImportService
import org.gradle.api.Named
import org.gradle.api.provider.Property

public abstract class ImportConfiguration : Named {

    public abstract val schemesDirName: Property<String>

    public abstract val importService: Property<SchemesImportService<*>>
}

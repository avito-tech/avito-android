package com.avito.module_api_extraction

import com.avito.android.module_type.ModuleType
import org.gradle.api.provider.Property

public abstract class ModuleApiExtractionExtension {

    /**
     * Specifies whether modules of certain types should be present in the task output.
     */
    public abstract val shouldModuleBeExamined: Property<(ModuleType) -> Boolean>
}

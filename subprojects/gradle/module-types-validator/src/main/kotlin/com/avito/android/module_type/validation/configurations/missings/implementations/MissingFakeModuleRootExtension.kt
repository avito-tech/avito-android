package com.avito.android.module_type.validation.configurations.missings.implementations

import org.gradle.api.provider.Property
import java.io.File

public abstract class MissingFakeModuleRootExtension {

    public abstract val ignoreLogicalModuleRegexes: Property<File>
}

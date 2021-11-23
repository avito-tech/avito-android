package com.avito.android.module_type

import org.gradle.api.provider.Property

public abstract class ModuleTypeExtension {

    public abstract val type: Property<ModuleType>
}

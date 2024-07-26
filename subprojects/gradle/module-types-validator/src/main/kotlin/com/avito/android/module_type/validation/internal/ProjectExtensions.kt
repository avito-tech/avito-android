package com.avito.android.module_type.validation.internal

import com.avito.android.module_type.ModuleTypeExtension
import com.avito.android.module_type.ModuleTypesPlugin
import com.avito.android.module_type.validation.ValidationExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

internal fun Project.hasModuleTypePlugin(): Boolean {
    return plugins.hasPlugin(ModuleTypesPlugin::class.java)
}

internal fun Project.moduleTypeExtension(): ModuleTypeExtension {
    return extensions.getByType()
}

internal fun ModuleTypeExtension.validationExtension(): ValidationExtension {
    return extensions.getByType()
}

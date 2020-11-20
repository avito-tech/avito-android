package com.avito.android

import org.gradle.api.plugins.ExtensionContainer

open class ModuleTypeExtension(
    var type: ModuleType = ModuleType.IMPLEMENTATION
)

// todo правда должен быть nullable?
inline val ExtensionContainer.moduleType
    get() = findByType(ModuleTypeExtension::class.java)

package com.avito.android

import org.gradle.api.plugins.ExtensionContainer

public open class ModuleTypeExtension(
    public var type: ModuleType = ModuleType.IMPLEMENTATION
)

public inline val ExtensionContainer.moduleType: ModuleTypeExtension?
    get() = findByType(ModuleTypeExtension::class.java)

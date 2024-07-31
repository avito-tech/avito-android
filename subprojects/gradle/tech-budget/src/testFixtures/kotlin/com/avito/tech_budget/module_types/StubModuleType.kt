package com.avito.tech_budget.module_types

import com.avito.android.module_type.ApplicationDeclaration
import com.avito.android.module_type.ModuleType

class StubModuleType(
    type: FunctionalType
) : ModuleType(StubApplication(), createType(type)) {

    override fun isEqualTo(other: ModuleType) = this == other

    override fun description(): String = this::class.java.simpleName
}

enum class FunctionalType {
    Library, Public, Impl
}

@Suppress("DEPRECATION")
fun createType(type: FunctionalType): com.avito.android.module_type.FunctionalType {
    return when (type) {
        FunctionalType.Impl -> com.avito.android.module_type.FunctionalType.Impl
        FunctionalType.Public -> com.avito.android.module_type.FunctionalType.Public
        FunctionalType.Library -> com.avito.android.module_type.FunctionalType.Library
    }
}

class StubApplication : ApplicationDeclaration {
    override val name: String
        get() = "test"
}

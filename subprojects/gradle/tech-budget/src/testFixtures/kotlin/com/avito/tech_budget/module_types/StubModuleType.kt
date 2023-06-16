package com.avito.tech_budget.module_types

import com.avito.android.module_type.ModuleType

class StubModuleType(
    val type: FunctionalType
) : ModuleType {

    override fun isEqualTo(other: ModuleType) = this == other

    override fun description(): String = this::class.java.simpleName
}

enum class FunctionalType {
    Library, Public, Impl
}

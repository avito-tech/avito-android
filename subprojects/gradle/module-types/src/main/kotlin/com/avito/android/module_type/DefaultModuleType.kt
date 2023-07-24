package com.avito.android.module_type

public open class DefaultModuleType(
    public val app: ApplicationDeclaration,
    public val type: FunctionalType
) : ModuleType {

    override fun isEqualTo(other: ModuleType): Boolean {
        check(other is DefaultModuleType) {
            "Unsupported ModuleType: ${other::class.java}"
        }
        return this.app == other.app && this.type == other.type
    }

    override fun description(): String {
        return "app ${app.name} (type ${type.name})"
    }
}

package com.avito.android.module_type

import java.io.Serializable

@Deprecated("Delete after avito migrates to ModuleType")
public open class DefaultModuleType(
    app: ApplicationDeclaration,
    type: FunctionalType
) : ModuleType(app, type)

public open class ModuleType(
    public val app: ApplicationDeclaration,
    public val type: FunctionalType
) : Serializable {

    /**
     * [ModuleType] instances are serialized.
     * It implicitly breaks singleton's contracts.
     * Thus, we want to make this issue explicit and force to implement a comparison
     */
    public open fun isEqualTo(other: ModuleType): Boolean {
        return this.app == other.app && this.type == other.type
    }

    public open fun description(): String {
        return "app ${app.name} (type ${type.name})"
    }
}

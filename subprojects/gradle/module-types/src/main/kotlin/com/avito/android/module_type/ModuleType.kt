package com.avito.android.module_type

import java.io.Serializable

public interface ModuleType : Serializable {

    /**
     * [ModuleType] instances are serialized.
     * It implicitly breaks singleton's contracts.
     * Thus, we want to make this issue explicit and force to implement a comparison
     */
    public fun isEqualTo(other: ModuleType): Boolean

    public fun description(): String
}

package com.avito.android.module_type.validation.configurations.missings.implementations.internal

import com.avito.android.module_type.FunctionalType

internal fun FunctionalType.asRegex(): Regex {
    return Regex("^(${name.lowercase()})(-.+)?$")
}

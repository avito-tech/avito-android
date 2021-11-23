package com.avito.plugin.internal

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal fun String?.hasContent(): Boolean {
    contract {
        returns(true) implies (this@hasContent != null)
    }

    if (isNullOrBlank()) return false
    if (this == "null") return false
    return true
}

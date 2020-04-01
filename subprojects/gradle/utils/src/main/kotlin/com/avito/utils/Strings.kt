package com.avito.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * у gradle специфичные toString() из property
 */
@OptIn(ExperimentalContracts::class)
fun String?.hasContent(): Boolean {
    contract {
        returns(true) implies (this@hasContent != null)
    }

    if (isNullOrBlank()) return false
    if (this == "null") return false
    return true
}

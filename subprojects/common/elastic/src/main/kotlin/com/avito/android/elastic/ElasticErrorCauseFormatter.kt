package com.avito.android.elastic

internal fun Throwable?.formatCauseForElastic(): String {
    val errorChain = generateSequence(seed = this, nextFunction = { it.cause })
    return errorChain.joinToString(separator = "\nCaused by: ") { it.message ?: "No message" }
}

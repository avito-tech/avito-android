package com.avito.android.elastic

internal fun Throwable?.formatCauseForElastic(): String {
    val errorChain = generateSequence(seed = this, nextFunction = { it.cause })
    return errorChain.joinToString(separator = "\nCaused by ") {
        val className = it.javaClass.simpleName
        val message = it.message
        if (message != null) {
            "$className: $message"
        } else {
            "$className with no message"
        }
    }
}

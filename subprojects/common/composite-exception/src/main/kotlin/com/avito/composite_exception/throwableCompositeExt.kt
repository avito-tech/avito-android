package com.avito.composite_exception

fun Throwable?.composeWith(throwable: Throwable?): Throwable? {
    return when {
        this != null -> {
            if (throwable != null) {
                val message = "${message}. \n ${throwable.message}"
                CompositeException(
                    message,
                    arrayOf(this, throwable)
                )
            } else {
                this
            }
        }
        else -> throwable
    }
}

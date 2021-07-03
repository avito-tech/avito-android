package com.avito.composite_exception

import java.io.PrintStream
import java.io.PrintWriter

public class CompositeException(
    message: String,
    private val throwables: Array<Throwable>
) : RuntimeException(message) {

    override fun printStackTrace() {
        if (throwables.isEmpty()) {
            super.printStackTrace()
        } else {
            throwables.forEach {
                it.printStackTrace()
            }
        }
    }

    override fun printStackTrace(s: PrintStream) {
        if (throwables.isEmpty()) {
            super.printStackTrace(s)
        } else {
            throwables.forEach {
                it.printStackTrace(s)
            }
        }
    }

    override fun printStackTrace(s: PrintWriter) {
        if (throwables.isEmpty()) {
            super.printStackTrace(s)
        } else {
            throwables.forEach {
                it.printStackTrace(s)
            }
        }
    }
}

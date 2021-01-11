package com.avito.logger

import java.io.Serializable

interface LoggingFormatter : Serializable {

    fun format(message: String): String
}

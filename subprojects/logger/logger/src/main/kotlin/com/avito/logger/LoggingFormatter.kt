package com.avito.logger

import java.io.Serializable

public interface LoggingFormatter : Serializable {

    public fun format(message: String): String
}

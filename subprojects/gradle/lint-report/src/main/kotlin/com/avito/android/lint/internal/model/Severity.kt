package com.avito.android.lint.internal.model

internal enum class Severity(val description: String) {

    FATAL("Fatal"),

    ERROR("Error"),

    WARNING("Warning"),

    INFORMATIONAL("Information"),

    IGNORE("Ignore")
}

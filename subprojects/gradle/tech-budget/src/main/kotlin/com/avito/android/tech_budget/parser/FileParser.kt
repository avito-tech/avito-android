package com.avito.android.tech_budget.parser

import java.io.File

public fun interface FileParser<T> {
    public fun parse(file: File): List<T>
}

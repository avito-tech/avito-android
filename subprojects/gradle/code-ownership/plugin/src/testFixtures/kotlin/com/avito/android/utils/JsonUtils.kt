package com.avito.android.utils

import com.fasterxml.jackson.databind.json.JsonMapper

fun String.compactPrintJson() = JsonMapper().readTree(this).toString()

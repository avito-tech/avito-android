package com.avito.android

import me.champeau.jdoctor.Problem
import me.champeau.jdoctor.exceptions.ExceptionUtils

fun <ID : Enum<ID>, SEVERITY : Enum<SEVERITY>, CONTEXT, PAYLOAD>
    Problem<ID, SEVERITY, CONTEXT, PAYLOAD>.asRuntimeException(): RuntimeException =
    ExceptionUtils.asRuntimeException<RuntimeException, ID, SEVERITY, CONTEXT, PAYLOAD>(this)

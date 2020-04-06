package com.avito.bytecode.metadata

data class ModulePath(val path: String) {
    init {
        require(path.matches(validModulePath)) { "$path is not a valid gradle module path" }
    }
}

fun ModulePath.toFileName() = path.drop(1).replace(":", "+")

private val validModulePath = Regex("(:[a-zA-Z\\-]+(_[a-zA-Z\\-])?)*")

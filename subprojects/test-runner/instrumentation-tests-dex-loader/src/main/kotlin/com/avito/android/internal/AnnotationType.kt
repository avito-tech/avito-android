package com.avito.android.internal

/**
 * @param type пример: "Lorg/junit/Test;"
 */
class AnnotationType(val type: String) {

    init {
        require(type.endsWith(';')) { "Invalid type definition: $type" }
    }
}

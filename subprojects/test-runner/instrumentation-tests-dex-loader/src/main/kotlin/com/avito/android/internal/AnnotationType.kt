package com.avito.android.internal

/**
 * @param type пример: "Lorg/junit/Test;"
 */
public class AnnotationType(public val type: String) {

    init {
        require(type.endsWith(';')) { "Invalid type definition: $type" }
    }
}

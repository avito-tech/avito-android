package com.avito.bytecode.invokes.test

import org.apache.bcel.classfile.Method
import org.apache.bcel.generic.MethodGen

fun Method.isTest(): Boolean = try {
    annotationEntries
        .find { it.annotationType == TEST_ANNOTATION } != null
} catch (t: Throwable) {
    false
}

fun MethodGen.isTest(): Boolean = try {
    annotationEntries
        .find { it.typeName == TEST_ANNOTATION } != null
} catch (t: Throwable) {
    false
}

fun MethodGen.isBefore(): Boolean = try {
    annotationEntries
        .find { it.typeName == BEFORE_ANNOTATION } != null
} catch (t: Throwable) {
    false
}

const val TEST_ANNOTATION = "Lorg/junit/Test;"
const val BEFORE_ANNOTATION = "Lorg/junit/Before;"

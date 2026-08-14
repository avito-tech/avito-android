package com.avito.android

private const val DEX_OBJECT_TYPE_PREFIX = 'L'

internal fun String.toJavaType(): String = if (startsWith(DEX_OBJECT_TYPE_PREFIX) && endsWith(';')) {
    substring(1, length - 1).replace('/', '.')
} else {
    throw IllegalStateException("Invalid dex object type")
}

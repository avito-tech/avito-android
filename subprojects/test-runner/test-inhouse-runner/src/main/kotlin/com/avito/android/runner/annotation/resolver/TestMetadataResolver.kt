package com.avito.android.runner.annotation.resolver

import java.io.Serializable

interface TestMetadataResolver {

    sealed class Resolution {
        data class ReplaceString(val replacement: String) : Resolution()
        data class ReplaceSerializable(val replacement: Serializable) : Resolution()
        data class NothingToChange(val reason: String) : Resolution()
    }

    val key: String

    fun resolve(test: TestMethodOrClass): Resolution
}

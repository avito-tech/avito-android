package com.avito.android.runner.annotation.resolver

import java.io.Serializable

public interface TestMetadataResolver {

    public sealed class Resolution {
        public data class ReplaceString(val replacement: String) : Resolution()
        public data class ReplaceSerializable(val replacement: Serializable) : Resolution()
        public data class NothingToChange(val reason: String) : Resolution()
    }

    public val key: String

    public fun resolve(test: TestMethodOrClass): Resolution
}

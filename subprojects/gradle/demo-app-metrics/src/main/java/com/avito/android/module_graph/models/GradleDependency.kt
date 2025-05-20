package com.avito.android.module_graph.models

import java.io.Serializable

public data class GradleDependency(
    val from: String,
    val to: String,
    val type: Type,
) : Serializable {

    public enum class Type(
        public val typeName: String
    ) : Serializable {
        Api("api"),
        Implementation("implementation"),
        CompileOnly("compileOnly"),
        RuntimeOnly("runtimeOnly"),
        TestApi("testApi"),
        TestImplementation("testImplementation"),
        AndroidTestImplementation("androidTestImplementation"),
        DebugImplementation("debugImplementation"),
        ReleaseImplementation("releaseImplementation"),
    }
}

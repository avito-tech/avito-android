package com.avito.android.module_type.validation.publicimpl.internal

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter

@JsonClass(generateAdapter = true)
internal class MissingPublicDependencies(
    val projectPath: String,
    val buildFilePath: String,
    val dependencies: Map<String, List<ProjectDependencyInfo>>
) {

    companion object {

        @OptIn(ExperimentalStdlibApi::class)
        fun adapter() = Moshi.Builder()
            .build()
            .adapter<MissingPublicDependencies>()
    }
}

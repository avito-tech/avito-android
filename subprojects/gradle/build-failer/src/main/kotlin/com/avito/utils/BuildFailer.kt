package com.avito.utils

import com.avito.android.Problem
import org.gradle.api.Project

@Suppress("unused")
public val Project.buildFailer: BuildFailer
    get() = RealFailer()

public interface BuildFailer {

    public fun failBuild(
        problem: Problem
    )

    public fun failBuild(
        message: String
    )

    public fun failBuild(
        message: String,
        cause: Throwable
    )
}

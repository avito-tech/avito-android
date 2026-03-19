package com.avito.android.string_transform.internal.task.input

import org.gradle.api.tasks.Input

internal data class TransformRuleInput(
    @get:Input
    val from: String,
    @get:Input
    val to: String,
)

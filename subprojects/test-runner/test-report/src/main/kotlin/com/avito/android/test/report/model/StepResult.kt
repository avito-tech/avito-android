package com.avito.android.test.report.model

/**
 * @param number step number; must be same as in test case
 * @param title in http://links.k.avito.ru/9O: "Step Action"; value must be the same (can be trimmed)
 */
public data class StepResult(
    /**
     * Synthetic step is created by the framework. It has simpler lifecycle then Step created by users
     * So we need that flag in the framework for making decisions:
     * - What we should do when we start a new step or precondition and current step isn't NULL?
     * If current step is Synthetic we will overwrite it.
     * If current step is Real we will fail because we try to create INTERNAL step
     */
    val isSynthetic: Boolean,
    override val title: String,
    val timestamp: Long,
    val number: Int,
    val attachments: StepAttachments = StepAttachments()
) : StepModel

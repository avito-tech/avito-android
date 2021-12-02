package com.avito.android.signer.internal

import com.avito.android.signer.AbstractSignTask
import com.avito.android.signer.SignExtension

internal class TaskConfigurator(
    private val extension: SignExtension,
    private val urlResolver: UrlResolver = UrlResolver(),
    private val defaultTimeoutSec: Long = 40L
) {

    fun configure(task: AbstractSignTask, token: String) {
        task.group = "ci"

        task.serviceUrl.set(
            urlResolver.resolveServiceUrl(
                url = extension.serviceUrl,
                taskPath = task.path
            )
        )

        task.tokenProperty.set(token)

        task.readWriteTimeoutSec.set(
            extension.readWriteTimeoutSec.convention(defaultTimeoutSec)
        )
    }
}

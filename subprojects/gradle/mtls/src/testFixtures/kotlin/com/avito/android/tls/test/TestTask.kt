package com.avito.android.tls.test

import com.avito.android.tls.credentials.TlsCredentialsFactory
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

@Suppress("unused")
abstract class TestTask : DefaultTask() {

    @get:Input
    abstract val credentialsFactory: Property<TlsCredentialsFactory>

    @TaskAction
    fun action() {
        val credentials = credentialsFactory.get().createCredentials()
        logger.warn("Crt: ${credentials.crt}")
        logger.warn("Key: ${credentials.key}")
    }
}

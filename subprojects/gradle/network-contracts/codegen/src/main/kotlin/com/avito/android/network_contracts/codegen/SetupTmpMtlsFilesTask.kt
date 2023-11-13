package com.avito.android.network_contracts.codegen

import com.avito.android.network_contracts.shared.throwGradleError
import com.avito.android.tls.TlsCredentialsService
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@CacheableTask
internal abstract class SetupTmpMtlsFilesTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    @get:OutputFile
    val tmpCrt: Property<RegularFile> = objects.fileProperty()

    @get:OutputFile
    val tmpKey: Property<RegularFile> = objects.fileProperty()

    @get:Internal
    internal abstract val loggerFactory: Property<LoggerFactory>

    @get:Internal
    internal abstract val tlsCredentialsService: Property<TlsCredentialsService>

    private val logger: Logger by lazy { loggerFactory.get().create("SetupTmpMtlsFilesTask") }

    @TaskAction
    fun setUp() {
        val credentials = tlsCredentialsService.get().createCredentials()

        val mtlsCrt = credentials.crt
        val mtlsKey = credentials.key

        val tmpCrtFile = tmpCrt.get().asFile
        val tmpKeyFile = tmpKey.get().asFile

        try {
            logger.info("Create temporary mTLS files...")
            tmpCrtFile.writeBytes(mtlsCrt.encodeToByteArray())
            tmpKeyFile.writeBytes(mtlsKey.encodeToByteArray())
        } catch (e: Throwable) {
            tmpCrtFile.delete()
            tmpKeyFile.delete()

            throwGradleError("Temporary mTLS files have not been created.", e)
        }
    }

    companion object {
        const val NAME: String = "setupTmpMtlsFiles"
    }
}

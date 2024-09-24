package com.avito.android.tls

import com.avito.android.tls.credentials.TlsCredentials
import com.avito.android.tls.credentials.TlsCredentialsFactory
import com.avito.android.tls.credentials.isValid
import com.avito.android.tls.exception.TlsCredentialsRetrievingInformation
import com.avito.android.tls.exception.TlsNotFoundException
import com.avito.android.tls.exception.TlsSetupException
import com.avito.android.tls.extensions.configuration.TlsCredentialsProviderConfiguration
import com.avito.android.tls.internal.provider.TlsCredentialsProviderFactory
import org.gradle.api.logging.Logger

public class TlsProjectCredentialsFactory(
    private val configurations: Set<TlsCredentialsProviderConfiguration>,
    private val docsUrl: String = "",
    private val verbose: Boolean = false,
    private val logger: Logger? = null
) : TlsCredentialsFactory {

    private val tlsCredentialsProviderFactory = TlsCredentialsProviderFactory()

    override fun createCredentials(): TlsCredentials {
        val configurationCredentials = configurations
            .asSequence()
            .map { configuration ->
                val credentialsProvider = tlsCredentialsProviderFactory.createProvider(configuration)
                ConfigurationCredentials(
                    configuration = configuration,
                    credentials = credentialsProvider.provideCredentials()
                )
            }

        val credentials = configurationCredentials.find { it.credentials.isValid }
        if (credentials != null) {
            if (verbose) {
                logger?.logAppliedCredentials(credentials.credentials)
            }
            return credentials.credentials
        }

        throwExceptionWithInstructions(configurationCredentials.asIterable())
    }

    private fun throwExceptionWithInstructions(configurationCredentials: Iterable<ConfigurationCredentials>): Nothing {
        if (configurations.isEmpty()) {
            val message = """
            Register providers by adding mTls configuration to you build.gradle file.
            Configuration:
                tls {
                    credentials {
                        registerProvider([provider])
                    }
                }
            """.trimIndent()
            throwSetupException(message)
        }

        val isEmptyConfiguration = configurationCredentials.all { it.credentials is TlsCredentials.Undefined }
        if (isEmptyConfiguration) {
            throwSetupException("Follow the instructions to set up mTLS: $docsUrl")
        }

        val retrievingInformation = configurationCredentials
            .mapNotNull { data ->
                val credentials = data.credentials
                if (credentials is TlsCredentials.NotFound) {
                    TlsCredentialsRetrievingInformation(
                        action = data.configuration.actionText.getOrElse(""),
                        problem = credentials.message,
                        solution = data.configuration.fallbackText.orNull,
                    )
                } else {
                    null
                }
            }

        val areCredentialsUndefined = configurationCredentials.all { it.credentials is TlsCredentials.Undefined }
        if (areCredentialsUndefined) {
            throwSetupException("Follow the instructions to set up mTLS: $docsUrl")
        }

        throwNotFoundException(retrievingInformation)
    }

    private data class ConfigurationCredentials(
        val configuration: TlsCredentialsProviderConfiguration,
        val credentials: TlsCredentials,
    )
}

private fun throwSetupException(message: String): Nothing {
    throw TlsSetupException(message = message)
}

private fun throwNotFoundException(
    retrievingInformation: List<TlsCredentialsRetrievingInformation> = emptyList(),
): Nothing {
    throw TlsNotFoundException(retrievingInformation)
}

private fun Logger.logAppliedCredentials(credentials: TlsCredentials) {
    val message = buildString {
        append("Applied tls configuration: ")
        when (credentials) {
            is TlsCredentials.PlainCredentials -> append("raw configured credentials")
            is TlsCredentials.NotFound -> append("credentials not found: ${credentials.message}")
            is TlsCredentials.Undefined -> append("credentials are undefined")
            is TlsCredentials.FileCredentials -> {
                appendLine("key: `${credentials.tlsKeyFile.path}`")
                appendLine("crt: `${credentials.tlsCrtFile.path}`")
            }
        }
    }
    lifecycle(message)
}

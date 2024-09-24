package com.avito.android.tls.test

import com.avito.android.tls.test.stubs.StubRawConfigurationData

private val defaultMtlsProvider = StubRawConfigurationData(
    crtContent = "crtContent",
    keyContent = "keyContent"
)

fun createMtlsExtensionString(
    providers: List<StubRawConfigurationData> = listOf(defaultMtlsProvider)
): String {
    return """
        tls { 
            credentials {
                ${createTlsCredentialsProviders(providers)}
            }
        }
    """.trimIndent()
}

private fun createTlsCredentialsProviders(
    stubProviders: List<StubRawConfigurationData>
): String {
    return stubProviders.joinToString(separator = "\n", transform = ::registerProvider)
}

private fun registerProvider(provider: StubRawConfigurationData): String {
    return """
            registerProvider(
                "${provider.name}",
                com.avito.android.tls.extensions.configuration.RawContentTlsCredentialsConfiguration::class.java
            ) { 
                crtContent.set("${provider.crtContent}")
                keyContent.set("${provider.keyContent}")
                helperText.set("${provider.actionText}")
            }
        """.trimIndent()
}

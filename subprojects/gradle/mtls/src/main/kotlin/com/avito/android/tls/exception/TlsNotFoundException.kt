package com.avito.android.tls.exception

internal class TlsNotFoundException(
    private val infoList: List<TlsCredentialsRetrievingInformation> = emptyList()
) : RuntimeException() {

    override val message: String = createMessage()

    private fun createMessage(): String {
        return buildString {
            appendLine("Unable to locate TLS credentials files.")

            if (infoList.isNotEmpty()) {
                appendRetrievingInformation(infoList)
            } else {
                appendMtlsConfigurationInformation()
            }
        }
    }
}

private fun StringBuilder.appendRetrievingInformation(infoList: List<TlsCredentialsRetrievingInformation>) {
    appendLine("To address this issue, consider one of the following options:")

    infoList.forEachIndexed { index, info ->
        appendLine("${index + 1}. ${info.info}")
    }
}

private fun StringBuilder.appendMtlsConfigurationInformation() {
    val message = """
            Register providers by adding mTls configuration to you build.gradle file.
            Configuration:
                tls {
                    credentials {
                        registerProvider([provider])
                    }
                }
        """.trimIndent()
    appendLine(message)
}

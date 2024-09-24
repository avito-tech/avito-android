package com.avito.android.tls.exception

internal class TlsNotFoundException(
    private val infoList: List<TlsCredentialsRetrievingInformation> = emptyList(),
) : RuntimeException() {

    override val message: String = createMessage()

    private fun createMessage(): String {
        return buildString {
            appendLine("Unable to locate TLS credentials files.")

            if (infoList.isNotEmpty()) {
                appendRetrievingInformation(infoList)
            }
        }
    }
}

private fun StringBuilder.appendRetrievingInformation(infoList: List<TlsCredentialsRetrievingInformation>) {
    appendLine()
    appendLine("----------- Applied configurations ------------")
    appendLine()

    infoList.forEachIndexed { index, info ->
        appendLine("${index + 1}. ${info.action}")
        appendLine("Problem: ${info.problem}")
        if (!info.solution.isNullOrBlank()) {
            appendLine("Solution: ${info.solution}")
        }
    }
}

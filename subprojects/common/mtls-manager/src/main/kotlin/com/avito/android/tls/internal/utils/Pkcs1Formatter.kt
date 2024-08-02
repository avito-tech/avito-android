package com.avito.android.tls.internal.utils

import java.nio.charset.Charset
import java.security.GeneralSecurityException
import java.util.Base64

private const val ENCRYPTION_PATTERN = "-----"
private const val PKCS_8_KEY_HEADER = "BEGIN PRIVATE KEY"
private const val PKCS_8_KEY_FOOTER = "END PRIVATE KEY"
private const val PKCS_1_KEY_HEADER = "BEGIN RSA PRIVATE KEY"
private const val PKCS_1_KEY_FOOTER = "END RSA PRIVATE KEY"

internal fun String.isPkcs8Key() = startsWith(ENCRYPTION_PATTERN + PKCS_8_KEY_HEADER)

/**
 * Функция для получения приватного ключа формата PKCS_1.
 * В jdk отсутствует стандартный механизм для PKCS_1.
 */
internal fun formatPkcs1toPkcs8(caKey: String): String {
    val privateKeyContent = caKey
        .replace(createCertificateHeader(PKCS_1_KEY_HEADER), "")
        .replace(System.lineSeparator().toRegex(), "")
        .replace(createCertificateHeader(PKCS_1_KEY_FOOTER), "")

    val decodedContent = Base64.getDecoder().decode(privateKeyContent)
    val pkcs8Content = convertByteArray(decodedContent)
    val encodedPkcs8Content = Base64.getEncoder().encode(pkcs8Content).toString(Charset.defaultCharset())
    return buildString {
        appendLine(createCertificateHeader(PKCS_8_KEY_HEADER))
        appendLine(encodedPkcs8Content)
        appendLine(createCertificateHeader(PKCS_8_KEY_FOOTER))
    }
}

private fun createCertificateHeader(header: String): String {
    return ENCRYPTION_PATTERN + header + ENCRYPTION_PATTERN
}

@Throws(GeneralSecurityException::class)
private fun convertByteArray(pkcs1Bytes: ByteArray): ByteArray {
    // We can't use Java internal APIs to parse ASN.1 structures, so we build a PKCS#8 key Java can understand
    val pkcs1Length = pkcs1Bytes.size
    val totalLength = pkcs1Length + 22
    val pkcs8Header = byteArrayOf(
        0x30,
        0x82.toByte(),
        (totalLength shr 8 and 0xff).toByte(),
        (totalLength and 0xff).toByte(), // Sequence + total length
        0x2,
        0x1,
        0x0, // Integer (0)
        0x30,
        0xD,
        0x6,
        0x9,
        0x2A,
        0x86.toByte(),
        0x48,
        0x86.toByte(),
        0xF7.toByte(),
        0xD,
        0x1,
        0x1,
        0x1,
        0x5,
        0x0, // Sequence: 1.2.840.113549.1.1.1, NULL
        0x4,
        0x82.toByte(),
        (pkcs1Length shr 8 and 0xff).toByte(),
        (pkcs1Length and 0xff).toByte()
    )
    return pkcs8Header + pkcs1Bytes
}

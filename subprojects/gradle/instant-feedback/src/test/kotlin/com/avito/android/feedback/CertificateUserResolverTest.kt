package com.avito.android.feedback

import com.avito.android.Result
import com.google.common.truth.Truth.assertThat
import okhttp3.tls.HeldCertificate
import okhttp3.tls.certificatePem
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

internal class CertificateUserResolverTest {

    private val now: Instant = Instant.parse("2026-09-18T10:00:00Z")

    @Test
    fun `personal certificate - login from common name`() {
        val login = resolve(personalCertificate(commonName = LOGIN))

        assertThat(login).isEqualTo(LOGIN)
    }

    @Test
    fun `certificate of another issuer - no login`() {
        val otherCa = certificateAuthority("some-intermediate-ca")
        val certificate = HeldCertificate.Builder()
            .commonName("some-service-proxy")
            .validFor(days = 30)
            .signedBy(otherCa)
            .build()

        assertThat(resolve(certificate)).isNull()
    }

    @Test
    fun `expired certificate - no login`() {
        val certificate = personalCertificate(
            commonName = LOGIN,
            notBefore = now.minus(200, ChronoUnit.DAYS),
            notAfter = now.minus(1, ChronoUnit.DAYS),
        )

        assertThat(resolve(certificate)).isNull()
    }

    @Test
    fun `not yet valid certificate - no login`() {
        val certificate = personalCertificate(
            commonName = LOGIN,
            notBefore = now.plus(1, ChronoUnit.DAYS),
            notAfter = now.plus(200, ChronoUnit.DAYS),
        )

        assertThat(resolve(certificate)).isNull()
    }

    @Test
    fun `certificate authority itself - no login`() {
        val ca = certificateAuthority(PERSONAL_CA)

        assertThat(resolve(ca)).isNull()
    }

    @Test
    fun `personal certificate among ca certificates - login found`() {
        val ca = certificateAuthority(PERSONAL_CA)
        val personal = HeldCertificate.Builder()
            .commonName(LOGIN)
            .validFor(days = 30)
            .signedBy(ca)
            .build()

        val pem = ca.certificatePem() + personal.certificatePem()

        assertThat(resolvePem(pem)).isEqualTo(LOGIN)
    }

    @Test
    fun `personal certificate with human name in common name - no login`() {
        assertThat(resolve(personalCertificate(commonName = "Firstname Lastname"))).isNull()
    }

    @Test
    fun `personal certificate with uppercase common name - no login`() {
        assertThat(resolve(personalCertificate(commonName = LOGIN.uppercase()))).isNull()
    }

    @Test
    fun `issuer of another generation - login still found`() {
        val ca = certificateAuthority("${PERSONAL_CA_PREFIX}9999")
        val certificate = HeldCertificate.Builder()
            .commonName(LOGIN)
            .validFor(days = 30)
            .signedBy(ca)
            .build()

        assertThat(resolve(certificate)).isEqualTo(LOGIN)
    }

    @Test
    fun `certificate is unavailable - failure carries the reason`() {
        val reason = IllegalStateException("no crt file")

        val result = CertificateUserResolver(certificatePem = { Result.Failure(reason) }, now = { now })
            .resolve()

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        assertThat((result as Result.Failure).throwable).isSameInstanceAs(reason)
    }

    @Test
    fun `common name is not a login - failure says so`() {
        val certificate = personalCertificate(commonName = "Firstname Lastname")

        assertThat(failureMessage(certificate)).contains("CN is not a login")
    }

    @Test
    fun `certificate of another issuer - failure says so`() {
        val certificate = HeldCertificate.Builder()
            .commonName(LOGIN)
            .validFor(days = 30)
            .signedBy(certificateAuthority("some-intermediate-ca"))
            .build()

        assertThat(failureMessage(certificate)).contains("issuer is not personal")
    }

    @Test
    fun `expired certificate - failure says so`() {
        val certificate = personalCertificate(
            commonName = LOGIN,
            notBefore = now.minus(200, ChronoUnit.DAYS),
            notAfter = now.minus(1, ChronoUnit.DAYS),
        )

        assertThat(failureMessage(certificate)).contains("expired or not yet valid")
    }

    @Test
    fun `garbage instead of certificate - failure says pem can not be parsed`() {
        assertThat(failureMessagePem("not a certificate at all")).contains("can't parse the certificate PEM")
    }

    @Test
    fun `empty pem - no login`() {
        assertThat(resolvePem("")).isNull()
    }

    @Test
    fun `garbage instead of certificate - no login`() {
        assertThat(resolvePem("not a certificate at all")).isNull()
    }

    private fun personalCertificate(
        commonName: String,
        notBefore: Instant = now.minus(1, ChronoUnit.DAYS),
        notAfter: Instant = now.plus(30, ChronoUnit.DAYS),
    ): HeldCertificate = HeldCertificate.Builder()
        .commonName(commonName)
        .validityInterval(notBefore.toEpochMilli(), notAfter.toEpochMilli())
        .signedBy(certificateAuthority(PERSONAL_CA))
        .build()

    private fun certificateAuthority(commonName: String): HeldCertificate = HeldCertificate.Builder()
        .commonName(commonName)
        .certificateAuthority(0)
        .validFor(days = 365)
        .build()

    private fun HeldCertificate.Builder.validFor(days: Long): HeldCertificate.Builder = validityInterval(
        now.minus(1, ChronoUnit.DAYS).toEpochMilli(),
        now.plus(days, ChronoUnit.DAYS).toEpochMilli(),
    )

    private fun resolve(certificate: HeldCertificate): String? = resolvePem(certificate.certificatePem())

    private fun resolvePem(pem: String): String? = resolveResult(pem)
        .fold(onSuccess = { it }, onFailure = { null })

    private fun failureMessage(certificate: HeldCertificate): String =
        failureMessagePem(certificate.certificatePem())

    private fun failureMessagePem(pem: String): String =
        (resolveResult(pem) as Result.Failure).throwable.message.orEmpty()

    private fun resolveResult(pem: String): Result<String> =
        CertificateUserResolver(certificatePem = { Result.Success(pem) }, now = { now }).resolve()

    private companion object {
        const val LOGIN = "testuser"

        /** The prefix is part of the contract under test, the generation is arbitrary */
        const val PERSONAL_CA_PREFIX = "personal-ca-"
        const val PERSONAL_CA = "${PERSONAL_CA_PREFIX}0000"
    }
}

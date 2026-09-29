package com.avito.android.feedback

import com.avito.android.Result
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.Instant
import javax.naming.ldap.LdapName
import javax.security.auth.x500.X500Principal

public class CertificateUserResolver(
    private val certificatePem: () -> Result<String>,
    private val now: () -> Instant = { Instant.now() },
) : UsernameResolver {

    override fun resolve(): Result<String> = certificatePem().flatMap { pem -> resolveLogin(pem, now()) }

    private fun resolveLogin(pem: String, now: Instant): Result<String> = pem.parseCertificates()
        .flatMap { parsed ->
            val certificates = parsed.filter { it.basicConstraints == NOT_A_CA }

            val login = certificates
                .asSequence()
                .filter { it.isIssuedPersonally() }
                .filter { it.isValidAt(now) }
                .mapNotNull { it.subjectX500Principal.commonName() }
                .firstOrNull { loginRegex.matches(it) }

            if (login != null) {
                Result.Success(login)
            } else {
                Result.Failure(IllegalStateException(noPersonalCertificateMessage(certificates, now)))
            }
        }

    private fun noPersonalCertificateMessage(certificates: List<X509Certificate>, now: Instant): String {
        if (certificates.isEmpty()) return "no certificates found in the PEM"

        val described = certificates.joinToString { certificate ->
            val subject = certificate.subjectX500Principal.commonName() ?: "?"
            val issuer = certificate.issuerX500Principal.commonName() ?: "?"
            val problem = when {
                !certificate.isIssuedPersonally() -> "issuer is not personal"
                !certificate.isValidAt(now) -> "expired or not yet valid"
                else -> "CN is not a login"
            }
            "CN=$subject, issuer=$issuer ($problem)"
        }
        return "no personal certificate among ${certificates.size}: $described"
    }

    private fun X509Certificate.isIssuedPersonally(): Boolean {
        val issuer = issuerX500Principal.commonName() ?: return false
        return personalIssuerPrefixes.any { issuer.startsWith(it) }
    }

    private fun String.parseCertificates(): Result<List<X509Certificate>> = Result.tryCatch {
        byteInputStream().use { stream ->
            CertificateFactory.getInstance("X.509")
                .generateCertificates(stream)
                .filterIsInstance<X509Certificate>()
        }
    }.rescue { error ->
        Result.Failure(IllegalStateException("can't parse the certificate PEM: ${error.message}", error))
    }

    private fun X509Certificate.isValidAt(now: Instant): Boolean =
        !now.isBefore(notBefore.toInstant()) && !now.isAfter(notAfter.toInstant())

    private fun X500Principal.commonName(): String? = runCatching {
        LdapName(getName(X500Principal.RFC2253)).rdns
            .firstOrNull { it.type.equals("CN", ignoreCase = true) }
            ?.value
            ?.toString()
    }.getOrNull()

    private companion object {

        val personalIssuerPrefixes = listOf("personal-ca-", "personal-certificate-")

        val loginRegex = Regex("^[a-z][a-z0-9_.-]{1,63}$")

        const val NOT_A_CA = -1
    }
}

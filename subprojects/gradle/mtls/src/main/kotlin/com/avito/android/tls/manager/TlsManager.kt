package com.avito.android.tls.manager

import com.avito.android.tls.credentials.TlsCredentials
import com.avito.android.tls.credentials.TlsCredentialsFactory
import com.avito.android.tls.credentials.pkcs8Key
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import okhttp3.tls.certificatePem
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

public class TlsManager(
    private val tlsCredentials: TlsCredentials,
) {

    public constructor(tlsCredentialsFactory: TlsCredentialsFactory) :
        this(tlsCredentialsFactory.createCredentials())

    public fun handshakeCertificates(): HandshakeCertificates {
        require(tlsCredentials.crt.isNotEmpty())
        require(tlsCredentials.key.isNotEmpty())

        val certificatesBuilder = HandshakeCertificates.Builder()

        tlsCredentials.crt.byteInputStream().use { inStream ->
            val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
            val certs = cf.generateCertificates(inStream).map { it as X509Certificate }

            val cert = certs.first()
            val intermediateCerts = mutableListOf<X509Certificate>()

            if (certs.size > 1) {
                certs.forEachIndexed { index, x509Certificate ->
                    if (index != 0) {
                        intermediateCerts.add(x509Certificate)
                    }
                }
            }

            val pemCertificateContent = cert.certificatePem() + tlsCredentials.pkcs8Key()
            val helpCertificates = HeldCertificate.decode(pemCertificateContent)

            certificatesBuilder.heldCertificate(
                helpCertificates,
                *intermediateCerts.toTypedArray()
            )
        }
        certificatesBuilder.addPlatformTrustedCertificates()
        return certificatesBuilder.build()
    }
}

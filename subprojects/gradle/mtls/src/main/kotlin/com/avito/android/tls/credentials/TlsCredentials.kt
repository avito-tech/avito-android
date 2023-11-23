package com.avito.android.tls.credentials

import com.avito.android.tls.internal.utils.formatPkcs1toPkcs8
import com.avito.android.tls.internal.utils.isPkcs8Key
import java.io.File

public sealed class TlsCredentials {

    public abstract val crt: String
    public abstract val key: String

    internal class PlainCredentials(
        override val crt: String,
        override val key: String,
    ) : TlsCredentials()

    internal class FileCredentials(
        private val tlsCrtFile: File,
        private val tlsKeyFile: File,
    ) : TlsCredentials() {

        override val crt: String by lazy { tlsCrtFile.readText() }
        override val key: String by lazy { tlsKeyFile.readText() }
    }

    internal object Undefined : TlsCredentials() {

        override val crt: String get() = error("Tls credentials is undefined")
        override val key: String get() = error("Tls credentials is undefined")
    }
}

internal fun TlsCredentials.pkcs8Key(): String {
    if (key.isPkcs8Key()) {
        return key
    }

    return formatPkcs1toPkcs8(key)
}

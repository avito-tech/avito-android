package com.avito.android.tls.credentials

import com.avito.android.tls.internal.utils.formatPkcs1toPkcs8
import com.avito.android.tls.internal.utils.isPkcs8Key
import java.io.File

public sealed class TlsCredentials {

    public abstract val crt: String
    public abstract val key: String

    public class PlainCredentials(
        override val crt: String,
        override val key: String,
    ) : TlsCredentials()

    public class FileCredentials(
        public val tlsCrtFile: File,
        public val tlsKeyFile: File,
    ) : TlsCredentials() {

        override val crt: String by lazy { tlsCrtFile.readText() }
        override val key: String by lazy { tlsKeyFile.readText() }
    }

    public sealed class Failure(
        public val message: String,
    ) : TlsCredentials() {

        override val crt: String get() = error(message)
        override val key: String get() = error(message)
    }

    public class NotFound(message: String) : Failure(message)

    public data object Undefined : Failure("Tls credentials is undefined")
}

public fun TlsCredentials.pkcs8Key(): String = pkcs8Key(key)

public fun pkcs8Key(key: String): String {
    if (key.isPkcs8Key()) {
        return key
    }

    return formatPkcs1toPkcs8(key)
}

public val TlsCredentials.isValid: Boolean
    get() = this != TlsCredentials.Undefined && this !is TlsCredentials.NotFound

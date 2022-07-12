package com.avito.deeplink_generator.model

import java.io.Serializable

public class Deeplink(
    public val scheme: String,
    public val host: String,
    public val path: String,
) : Serializable {

    override fun hashCode(): Int {
        var result = scheme.hashCode()
        result = 31 * result + host.hashCode()
        result = 31 * result + path.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Deeplink

        if (scheme != other.scheme) return false
        if (host != other.host) return false
        if (path != other.path) return false

        return true
    }

    override fun toString(): String {
        return "$scheme://$host$path"
    }
}

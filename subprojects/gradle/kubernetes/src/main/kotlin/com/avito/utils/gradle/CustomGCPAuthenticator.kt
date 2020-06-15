package com.avito.utils.gradle

import com.google.auth.oauth2.GoogleCredentials
import io.kubernetes.client.util.authenticators.GCPAuthenticator

/**
 * Default [GCPAuthenticator] doesn't able to refresh token
 * https://github.com/kubernetes-client/java/issues/290#issuecomment-480205118
 */
class CustomGCPAuthenticator(
    private val credentials: GoogleCredentials
) : GCPAuthenticator() {

    override fun refresh(config: MutableMap<String, Any>): MutableMap<String, Any> {
        val accessToken = credentials.refreshAccessToken()
        config["access-token"] = accessToken.tokenValue
        config["expiry"] = accessToken.expirationTime
        return config
    }
}

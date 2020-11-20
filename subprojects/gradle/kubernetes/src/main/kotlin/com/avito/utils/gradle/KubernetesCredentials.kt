package com.avito.utils.gradle

import java.io.Serializable

sealed class KubernetesCredentials : Serializable {

    object Empty : KubernetesCredentials(), Serializable

    data class Service(
        val token: String,
        val caCertData: String,
        val url: String
    ) : KubernetesCredentials(), Serializable

    data class Config(
        val context: String,
        val caCertFile: String? = kubeDefaultCaCertFile,
        val configFile: String = kubeConfigDefaultPath
    ) : KubernetesCredentials(), Serializable
}

// TODO: get rid of this default. autoConfig is enabled by default
private val kubeConfigDefaultPath: String by lazy {
    val userHome: String = requireUserHome()
    "${userHome}/.kube/config"
}

private val kubeDefaultCaCertFile: String by lazy {
    val userHome: String = requireUserHome()
    "${userHome}/.kube/avito_ca.crt"
}

private fun requireUserHome(): String {
    val userHome: String? = System.getProperty("user.home")
    require(!userHome.isNullOrBlank()) { "system property 'user.home' is not set" }
    return userHome
}

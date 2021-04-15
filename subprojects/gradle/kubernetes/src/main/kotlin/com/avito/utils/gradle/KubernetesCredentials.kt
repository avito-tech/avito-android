package com.avito.utils.gradle

import java.io.File
import java.io.Serializable

sealed class KubernetesCredentials : Serializable {

    object Empty : KubernetesCredentials() {

        override fun toString(): String = "KubernetesCredentials.Empty"
    }

    class Service(
        val token: String,
        val caCertData: String,
        val url: String
    ) : KubernetesCredentials() {

        override fun toString(): String = "KubernetesCredentials.Service"
    }

    class Config(
        val context: String,
        val caCertFile: File? = kubeDefaultCaCertFile,
        val configFile: File = kubeConfigDefaultPath
    ) : KubernetesCredentials() {

        override fun toString(): String = "KubernetesCredentials.Config"
    }
}

private val kubernetesHome: File by lazy {
    val userHome: String = requireUserHome()
    File(userHome, ".kube")
}

// TODO: get rid of this default. autoConfig is enabled by default
private val kubeConfigDefaultPath: File by lazy {
    File(kubernetesHome, "config")
}

private val kubeDefaultCaCertFile: File by lazy {
    File(kubernetesHome, "avito_ca.crt")
}

private fun requireUserHome(): String {
    val userHome: String? = System.getProperty("user.home")
    require(!userHome.isNullOrBlank()) { "system property 'user.home' is not set" }
    return userHome
}

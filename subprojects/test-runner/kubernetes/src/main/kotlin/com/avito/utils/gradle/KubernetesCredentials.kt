package com.avito.utils.gradle

import java.io.File
import java.io.Serializable

// todo used in gradle scripts; remove it from there
public sealed class KubernetesCredentials : Serializable {

    public object Empty : KubernetesCredentials() {

        override fun toString(): String = "KubernetesCredentials.Empty"
    }

    public class Service(
        public val token: String,
        public val caCertData: String,
        public val url: String,
        public val namespace: String,
    ) : KubernetesCredentials() {

        override fun toString(): String = "KubernetesCredentials.Service"
    }

    public class Config(
        public val context: String,
        public val namespace: String,
        public val caCertFile: File? = kubeDefaultCaCertFile,
        public val configFile: File = kubeConfigDefaultPath
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

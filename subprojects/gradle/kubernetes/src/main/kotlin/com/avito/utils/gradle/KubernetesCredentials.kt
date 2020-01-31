package com.avito.utils.gradle

import org.gradle.api.Project
import java.io.Serializable

interface KubernetesCredentials : Serializable {
    val token: String
    val caCertData: String
    val url: String

    class Implementation(project: Project) : KubernetesCredentials, Serializable {
        override val token: String by lazy {
            project.properties["kubernetesToken"]?.toString() ?: ""
        }

        override val caCertData: String by lazy {
            project.properties["kubernetesCaCertData"]?.toString() ?: ""
        }

        override val url: String by lazy {
            project.properties["kubernetesUrl"]?.toString() ?: ""
        }
    }
}

val Project.kubernetesCredentials: KubernetesCredentials
    get() = KubernetesCredentials.Implementation(this)

package com.avito.utils.gradle

import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Project
import java.io.Serializable

data class KubernetesCredentials(
    val token: String,
    val caCertData: String,
    val url: String
) : Serializable

val Project.kubernetesCredentials: KubernetesCredentials
    get() = KubernetesCredentials(
        token = this.getMandatoryStringProperty("kubernetesToken"),
        caCertData = this.getMandatoryStringProperty("kubernetesCaCertData"),
        url = this.getMandatoryStringProperty("kubernetesUrl")
    )

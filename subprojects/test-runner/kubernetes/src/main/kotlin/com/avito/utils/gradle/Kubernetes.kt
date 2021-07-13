@file:JvmName("Kubernetes")

package com.avito.utils.gradle

import com.avito.kotlin.dsl.getOptionalStringProperty
import org.gradle.api.Project
import java.io.File

// todo used in gradle scripts; remove it from there
public val Project.kubernetesCredentials: KubernetesCredentials
    get() {
        val context = getOptionalStringProperty("kubernetesContext", nullIfBlank = true)
        return if (context.isNullOrBlank()) {
            val token = getOptionalStringProperty("kubernetesToken", nullIfBlank = true)
            val caCertData = getOptionalStringProperty("kubernetesCaCertData", nullIfBlank = true)
            val url = getOptionalStringProperty("kubernetesUrl", nullIfBlank = true)
            if (token != null && caCertData != null && url != null) {
                KubernetesCredentials.Service(
                    token = token,
                    caCertData = caCertData,
                    url = url
                )
            } else {
                KubernetesCredentials.Empty
            }
        } else {
            val caCertFile = getOptionalStringProperty("kubernetesCaCertFile", nullIfBlank = true)?.let {
                File(it)
            }
            KubernetesCredentials.Config(context, caCertFile = caCertFile)
        }
    }

@file:JvmName("Kubernetes")

package com.avito.utils.gradle

import org.gradle.api.Project

// todo used in gradle scripts; remove it from there
public val Project.kubernetesCredentials: KubernetesCredentials
    get() = KubernetesCredentials.Empty

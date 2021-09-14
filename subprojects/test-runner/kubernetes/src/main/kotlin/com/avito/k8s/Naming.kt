package com.avito.k8s

/**
 * Refer to official rules: https://kubernetes.io/docs/concepts/overview/working-with-objects/names/
 */
public fun String.toValidKubernetesName(): String = replace("_", "-").lowercase()

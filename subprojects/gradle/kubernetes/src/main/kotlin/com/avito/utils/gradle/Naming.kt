package com.avito.utils.gradle

/**
 * if you encounter any problems with generated named, feel free to add more strict checks and changes
 *
 * Refer to official rules: https://kubernetes.io/docs/concepts/overview/working-with-objects/names/
 */
fun String.toValidKubernetesName(): String = replace("_", "-").toLowerCase()

plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(libs.kubernetesClient)
    api(projects.subprojects.common.teamcityCommon)
    api(libs.kotlinXCli)
}

tasks.register("clearByNamespaces", JavaExec::class.java) {
    mainClass.set("com.avito.ci.ClearK8SDeploymentsMain")
    classpath = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).runtimeClasspath
    doFirst {
        project.hasProperty("teamcityUrl")
        project.hasProperty("teamcityApiUser")
        project.hasProperty("teamcityApiPassword")
        project.hasProperty("kubernetesToken")
        project.hasProperty("kubernetesUrl")
        project.hasProperty("kubernetesCaCertData")
    }
    args(
        "clearByNamespaces",
        "--teamcityUrl", project.getOptionalStringProperty("teamcityUrl", ""),
        "--teamcityApiUser", project.getOptionalStringProperty("teamcityApiUser", ""),
        "--teamcityApiPassword", project.getOptionalStringProperty("teamcityApiPassword", ""),
        "--kubernetesToken", project.getOptionalStringProperty("kubernetesToken", ""),
        "--kubernetesUrl", project.getOptionalStringProperty("kubernetesUrl", ""),
        "--kubernetesCaCert", project.getOptionalStringProperty("kubernetesCaCertData", ""),
        "--namespaces", "android-emulator"
    )
}
tasks.register("deleteByNames", JavaExec::class.java) {
    mainClass.set("com.avito.ci.ClearK8SDeploymentsMain")
    classpath = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).runtimeClasspath
    doFirst {
        project.hasProperty("teamcityUrl")
        project.hasProperty("teamcityApiUser")
        project.hasProperty("teamcityApiPassword")
        project.hasProperty("kubernetesToken")
        project.hasProperty("kubernetesUrl")
        project.hasProperty("kubernetesCaCertData")
    }
    args(
        "deleteByNames",
        "--teamcityUrl",
        project.getOptionalStringProperty("teamcityUrl", ""),
        "--teamcityApiUser",
        project.getOptionalStringProperty("teamcityApiUser", ""),
        "--teamcityApiPassword",
        project.getOptionalStringProperty("teamcityApiPassword", ""),
        "--kubernetesToken",
        project.getOptionalStringProperty("kubernetesToken", ""),
        "--kubernetesUrl",
        project.getOptionalStringProperty("kubernetesUrl", ""),
        "--kubernetesCaCert",
        project.getOptionalStringProperty("kubernetesCaCertData", ""),
        "--namespace",
        project.getOptionalStringProperty("avito.k8s-deploymetns-cleaner.byNames.namespace", "android-emulator"),
        "--deploymentNames",
        project.getOptionalStringProperty("avito.k8s-deploymetns-cleaner.byNames.deploymentNames", "")
    )
}

fun Project.getOptionalStringProperty(name: String, nullIfBlank: Boolean = false): String? =
    if (hasProperty(name)) {
        val string = property(name)?.toString()
        if (nullIfBlank && string.isNullOrBlank()) null else string
    } else {
        null
    }

fun Project.getOptionalStringProperty(name: String, default: String, defaultIfBlank: Boolean = true): String =
    getOptionalStringProperty(name, nullIfBlank = defaultIfBlank) ?: default

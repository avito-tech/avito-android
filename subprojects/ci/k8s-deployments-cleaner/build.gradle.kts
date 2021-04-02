plugins {
    id("convention.kotlin-jvm")
    id("convention.libraries")
}

dependencies {
    api(libs.kubernetesClient)
    api(projects.common.teamcityCommon)
    api(libs.kotlinXCli)
}

// todo add if ci
// if(project.buildEnvironment is com.avito.utils.gradle.BuildEnvironment.CI) {
if (project.getOptionalStringProperty("ci", "false").toBoolean()) {
    tasks.register("clearByNamespaces", JavaExec::class.java) {
        main = "com.avito.ci.ClearK8SDeploymentsMain"
        classpath = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).runtimeClasspath
        args(
            "clearByNamespaces",
            "--teamcityUrl", project.getMandatoryStringProperty("teamcityUrl"),
            "--teamcityApiUser", project.getMandatoryStringProperty("teamcityApiUser"),
            "--teamcityApiPassword", project.getMandatoryStringProperty("teamcityApiPassword"),
            "--kubernetesToken", project.getMandatoryStringProperty("kubernetesToken"),
            "--kubernetesUrl", project.getMandatoryStringProperty("kubernetesUrl"),
            "--kubernetesCaCert", project.getMandatoryStringProperty("kubernetesCaCertData"),
            "--namespaces", "android-emulator"
        )
    }
    tasks.register("deleteByNames", JavaExec::class.java) {
        main = "com.avito.ci.ClearK8SDeploymentsMain"
        classpath = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).runtimeClasspath
        args(
            "deleteByNames",
            "--teamcityUrl",
            project.getMandatoryStringProperty("teamcityUrl"),
            "--teamcityApiUser",
            project.getMandatoryStringProperty("teamcityApiUser"),
            "--teamcityApiPassword",
            project.getMandatoryStringProperty("teamcityApiPassword"),
            "--kubernetesToken",
            project.getMandatoryStringProperty("kubernetesToken"),
            "--kubernetesUrl",
            project.getMandatoryStringProperty("kubernetesUrl"),
            "--kubernetesCaCert",
            project.getMandatoryStringProperty("kubernetesCaCertData"),
            "--namespace",
            project.getOptionalStringProperty("avito.k8s-deploymetns-cleaner.byNames.namespace", "android-emulator"),
            "--deploymentNames",
            project.getOptionalStringProperty("avito.k8s-deploymetns-cleaner.byNames.deploymentNames", "")
        )
    }
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

fun Project.getMandatoryStringProperty(name: String, allowBlank: Boolean = true): String {
    return if (hasProperty(name)) {
        val string = property(name)?.toString()
        if (string.isNullOrBlank()) {
            if (allowBlank) {
                ""
            } else {
                throw RuntimeException("Parameter: $name is blank but required")
            }
        } else {
            string
        }
    } else {
        throw RuntimeException("Parameter: $name is missing but required")
    }
}

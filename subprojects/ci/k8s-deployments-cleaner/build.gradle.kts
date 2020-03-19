import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalStringProperty

//import com.avito.utils.gradle.buildEnvironment

buildscript {
    val infraVersion: String by project
    dependencies {
        classpath("${Dependencies.gradle.avito.kotlinDslSupport}:$infraVersion")
//        classpath("${Dependencies.gradle.avito.buildEnvironment}:$infraVersion")
    }
}
plugins {
    id("kotlin")
}

dependencies {
    api(Dependencies.kubernetesClient)
    api(project(":subprojects:common:teamcity-common"))
    api(Dependencies.kotlinXCli)
}

// todo add if ci
//if(project.buildEnvironment is com.avito.utils.gradle.BuildEnvironment.CI) {
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
            "--namespaces", "android-emulator,android-performance"
        )
    }
    tasks.register("deleteByNames", JavaExec::class.java) {
        main = "com.avito.ci.ClearK8SDeploymentsMain"
        classpath = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).runtimeClasspath
        args(
            "deleteByNames",
            "--teamcityUrl", project.getMandatoryStringProperty("teamcityUrl"),
            "--teamcityApiUser", project.getMandatoryStringProperty("teamcityApiUser"),
            "--teamcityApiPassword", project.getMandatoryStringProperty("teamcityApiPassword"),
            "--kubernetesToken", project.getMandatoryStringProperty("kubernetesToken"),
            "--kubernetesUrl", project.getMandatoryStringProperty("kubernetesUrl"),
            "--kubernetesCaCert", project.getMandatoryStringProperty("kubernetesCaCertData"),
            "--namespace", project.getOptionalStringProperty("avito.k8s-deploymetns-cleaner.byNames.namespace", "android-emulator"),
            "--deploymentNames", project.getOptionalStringProperty("avito.k8s-deploymetns-cleaner.byNames.deploymentNames", "")
        )
    }
}
//}
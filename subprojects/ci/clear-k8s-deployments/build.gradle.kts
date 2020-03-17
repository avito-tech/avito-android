import com.avito.kotlin.dsl.getMandatoryStringProperty

buildscript {
    val infraVersion: String by project
    dependencies {
        classpath("${Dependencies.gradle.avito.kotlinDslSupport}:$infraVersion")
//        classpath("${Dependencies.gradle.avito.utils}:$infraVersion")
    }
}
plugins {
    id("kotlin")
}

dependencies {
    api(Dependencies.kubernetesClient)
    api(project(":subprojects:common:teamcity-common"))
    api("org.jetbrains.kotlinx:kotlinx-cli:0.2.1")
}

// todo add if ci
//if(project.buildEnvironment is BuildEnvironment.CI) {
tasks.register("run", JavaExec::class.java) {
    main = "com.avito.ci.ClearK8SDeployments"
    classpath = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).runtimeClasspath
    args(
        "--teamcityUrl", project.getMandatoryStringProperty("teamcityUrl"),
        "--teamcityApiUser", project.getMandatoryStringProperty("teamcityApiUser"),
        "--teamcityApiPassword", project.getMandatoryStringProperty("teamcityApiPassword"),
        "--kubernetesToken", project.getMandatoryStringProperty("kubernetesToken"),
        "--kubernetesUrl", project.getMandatoryStringProperty("kubernetesUrl"),
        "--kubernetesCaCert", project.getMandatoryStringProperty("kubernetesCaCertData"),
        "--namespaces", "android-emulator"
    )
}
//}
plugins {
    id("convention.publish-base")
}

val artifactoryUrlProperty: Provider<String> = providers.gradleProperty("artifactoryUrl")

val artifactoryUser: Provider<String> = providers.gradleProperty("avito.artifactory.user")

val artifactoryPassword: Provider<String> = providers.gradleProperty("avito.artifactory.password")

val repoName = "Artifactory"

val publishTask = tasks.register<Task>("publishToArtifactory") {
    group = "publication"

    dependsOn(tasks.named("publishAllPublicationsTo${repoName}Repository"))
}

publishing {
    repositories {
        maven {
            name = repoName
            val artifactoryUrl = artifactoryUrlProperty.orNull
            if (!artifactoryUrl.isNullOrBlank()) {
                setUrl("${artifactoryUrl}libs-release-local")
            }
            credentials {
                username = artifactoryUser.orNull
                password = artifactoryPassword.orNull
            }

            isAllowInsecureProtocol = true
        }
    }
}

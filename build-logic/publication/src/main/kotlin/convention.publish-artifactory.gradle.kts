plugins {
    id("convention.publish")
}

@Suppress("UnstableApiUsage")
val artifactoryUrl: Provider<String> = providers.gradleProperty("artifactoryUrl").forUseAtConfigurationTime()

/**
 * used in ci/publish_local.sh
 */
tasks.register<Task>("publishToArtifactory") {
    group = "publication"
    doFirst {
        requireNotNull(artifactoryUrl.orNull) {
            "Property artifactoryUrl is required for publishing"
        }
    }

    if (!artifactoryUrl.orNull.isNullOrBlank()) {
        dependsOn(tasks.named("publishAllPublicationsToArtifactoryRepository"))
    }
}

if (!artifactoryUrl.orNull.isNullOrBlank()) {
    publishing {
        repositories {
            maven {
                name = "Artifactory"
                setUrl("${artifactoryUrl.orNull}/libs-release-local")

                @Suppress("UnstableApiUsage")
                credentials {

                    username = providers.gradleProperty("avito.artifactory.user")
                        .forUseAtConfigurationTime()
                        .orNull

                    password = providers.gradleProperty("avito.artifactory.password")
                        .forUseAtConfigurationTime()
                        .orNull
                }
            }
        }
    }
}

plugins {
    `maven-publish`
}

group = "com.avito.android"

@Suppress("UnstableApiUsage")
val artifactoryUrl: Provider<String> = providers.gradleProperty("artifactoryUrl").forUseAtConfigurationTime()

val publishToArtifactoryTask = tasks.register<Task>("publishToArtifactory") {
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

publishing {
    repositories {
        if (!artifactoryUrl.orNull.isNullOrBlank()) {
            maven {
                name = "artifactory"
                setUrl("${artifactoryUrl.orNull}/libs-release-local")
                credentials {
                    username = project.getOptionalExtra("avito.artifactory.user")
                    password = project.getOptionalExtra("avito.artifactory.password")
                }
            }
        }
    }
}

fun Project.getOptionalExtra(key: String): String? {
    return if (extra.has(key)) {
        (extra[key] as? String)?.let { if (it.isBlank()) null else it }
    } else {
        null
    }
}

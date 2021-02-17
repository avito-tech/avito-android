plugins {
    id("convention.publish-base")
}

/**
 * used in ci/publish_local.sh
 */
    tasks.register<Task>("publishToSonatype") {
    group = "publication"

    if (isSnapshot()) {
        dependsOn(tasks.named("publishAllPublicationsToSonatypeSnapshotsRepository"))
    } else {
        dependsOn(tasks.named("publishAllPublicationsToSonatypeReleasesRepository"))
    }
}

publishing {
    repositories {
        maven {
            name = "SonatypeSnapshots"
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
            setupCredentials()
        }

        maven {
            name = "SonatypeReleases"
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            setupCredentials()
        }
    }
}

@Suppress("UnstableApiUsage")
fun MavenArtifactRepository.setupCredentials() {
    credentials {
        username = providers.gradleProperty("avito.ossrh.user")
            .forUseAtConfigurationTime()
            .orNull

        password = providers.gradleProperty("avito.ossrh.password")
            .forUseAtConfigurationTime()
            .orNull
    }
}

fun isSnapshot(): Boolean = version.toString().contains("SNAPSHOT", ignoreCase = true)

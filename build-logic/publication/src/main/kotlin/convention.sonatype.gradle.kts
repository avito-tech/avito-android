@file:Suppress("UnstableApiUsage")

plugins {
    id("convention.publish-base")
    signing
}

val publishTask = tasks.register<Task>("publishToSonatype") {
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

signing {
    sign(publishing.publications)

    val signingKeyId = providers.gradleProperty("avito.pgp.keyid")
        .forUseAtConfigurationTime()
        .orNull
    val signingKey = providers.gradleProperty("avito.pgp.key")
        .forUseAtConfigurationTime()
        .orNull
    val signingPassword = providers.gradleProperty("avito.pgp.password")
        .forUseAtConfigurationTime()
        .orNull

    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
}

tasks.withType<Sign>().configureEach {
    onlyIf {
        gradle.taskGraph.hasTask(publishTask.get())
    }
}

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

import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig

plugins {
    id("com.jfrog.bintray")
    id("convention.publish")
}

@Suppress("UnstableApiUsage")
val projectVersion: Provider<String> = providers.gradleProperty("projectVersion").forUseAtConfigurationTime()

val publishReleaseTaskName = "publishRelease"

val finalProjectVersion: String = System.getProperty("avito.project.version").let { env ->
    if (env.isNullOrBlank()) projectVersion.get() else env
}

group = "com.avito.android"
version = finalProjectVersion

/**
 * https://www.jetbrains.com/help/teamcity/build-script-interaction-with-teamcity.html#BuildScriptInteractionwithTeamCity-ReportingBuildNumber
 */
val teamcityPrintVersionTask: TaskProvider<Task> = tasks.register("teamcityPrintReleasedVersion") {
    group = "publication"
    description = "Prints teamcity service message to display released version as build number"

    doLast {
        logger.lifecycle("##teamcity[buildNumber '$finalProjectVersion']")
    }
}

tasks.register(publishReleaseTaskName) {
    group = "publication"
    finalizedBy(teamcityPrintVersionTask)
}

afterEvaluate {
    val registeredPublications = publishing.publications.names

    tasks.named(publishReleaseTaskName).configure {
        dependsOn(tasks.named("bintrayUpload"))
    }

    bintray {
        // todo fail fast with meaningful error message
        user = getOptionalExtra("avito.bintray.user")
        key = getOptionalExtra("avito.bintray.key")

        setPublications(*registeredPublications.toTypedArray())

        dryRun = false
        publish = true
        // You can use override for inconsistently uploaded artifacts
        // Examples of issues:
        // - NoHttpResponseException: api.bintray.com:443 failed to respond
        //   (https://github.com/bintray/gradle-bintray-plugin/issues/325)
        // - Could not upload to 'https://api.bintray.com/...':
        //   HTTP/1.1 405 Not Allowed 405 Not Allowed405 Not Allowednginx
        override = false

        pkg(
            closureOf<PackageConfig> {
                repo = "maven"
                userOrg = "avito"
                name = "avito-android"
                setLicenses("mit")
                vcsUrl = "https://github.com/avito-tech/avito-android.git"

                version(
                    closureOf<VersionConfig> {
                        name = finalProjectVersion
                    }
                )
            }
        )
    }
}

fun Project.getOptionalExtra(key: String): String? {
    return if (extra.has(key)) {
        (extra[key] as? String)?.let { if (it.isBlank()) null else it }
    } else {
        null
    }
}

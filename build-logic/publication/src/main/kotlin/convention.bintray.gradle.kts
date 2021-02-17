import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig

plugins {
    id("com.jfrog.bintray")
    id("convention.publish")
}

afterEvaluate {
    val registeredPublications = publishing.publications.names

    bintray {
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
                        name = version.toString()
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

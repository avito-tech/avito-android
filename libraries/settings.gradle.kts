rootProject.name = "libraries"

pluginManagement {
    val artifactoryUrl: String? by settings

    fun MavenArtifactRepository.artifactoryUrl(repositoryName: String) {
        setUrl("$artifactoryUrl/$repositoryName")
        isAllowInsecureProtocol = true
    }

    fun MavenArtifactRepository.setUrlOrProxy(repositoryName: String, originalRepo: String) {
        if (artifactoryUrl.isNullOrBlank()) {
            name = repositoryName
            setUrl(originalRepo)
        } else {
            name = "Proxy for $repositoryName: $originalRepo"
            artifactoryUrl(repositoryName)
        }
    }

    repositories {
        maven {
            setUrlOrProxy("gradle-plugins", "https://plugins.gradle.org/m2/")
        }
    }
}

val artifactoryUrl: String? by settings

fun MavenArtifactRepository.artifactoryUrl(repositoryName: String) {
    setUrl("$artifactoryUrl/$repositoryName")
    isAllowInsecureProtocol = true
}

fun MavenArtifactRepository.setUrlOrProxy(repositoryName: String, originalRepo: String) {
    if (artifactoryUrl.isNullOrBlank()) {
        name = repositoryName
        setUrl(originalRepo)
    } else {
        name = "Proxy for $repositoryName: $originalRepo"
        artifactoryUrl(repositoryName)
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        maven {
            setUrlOrProxy("mavenCentral", "https://repo1.maven.org/maven2")
        }
    }
}

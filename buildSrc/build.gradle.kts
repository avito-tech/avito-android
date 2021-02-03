plugins {
    `kotlin-dsl`
}

group = "com.avito.android"

val artifactoryUrl: String? by project

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
        setUrlOrProxy("jcenter", "https://jcenter.bintray.com")
    }
}

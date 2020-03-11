plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.sentry)
//    todo remove after merge implementation("com.fasterxml.jackson.core:jackson-core:2.10.0")
}

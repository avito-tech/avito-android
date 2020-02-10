plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val kotlinVersion: String by project
val okhttpVersion: String by project
val truthVersion: String by project

dependencies {
    api("com.squareup.okhttp3:mockwebserver:$okhttpVersion")

    implementation("com.google.truth:truth:$truthVersion")
}

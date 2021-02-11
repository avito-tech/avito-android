plugins {
    id("com.avito.android.kotlin-jvm")
}

dependencies {
    api(project(":subprojects:gradle:artifactory-app-backup"))

    implementation(project(":subprojects:common:test-okhttp"))
}

plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(gradleApi())

    testImplementation(project(":subprojects:gradle:test-project"))
}

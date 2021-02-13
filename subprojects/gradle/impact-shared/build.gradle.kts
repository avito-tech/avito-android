plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(libs.funktionaleTry)

    implementation(gradleApi())

    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:process"))

    implementation(libs.antPattern)
    implementation(libs.kotlinPlugin)

    testImplementation(project(":subprojects:gradle:git-test-fixtures"))
    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:common:logger")))

    testImplementation(libs.mockitoKotlin)
}

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(libs.funktionaleTry)
    api(project(":subprojects:gradle:module-dependencies-tree"))

    implementation(gradleApi())

    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:process"))

    implementation(libs.antPattern)
    implementation(libs.kotlinPlugin)

    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(project(":subprojects:gradle:git-test-fixtures"))
    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:common:logger")))

    testImplementation(libs.mockitoKotlin)
}

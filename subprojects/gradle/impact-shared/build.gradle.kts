plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":subprojects:gradle:module-dependencies"))

    implementation(gradleApi())

    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:process"))

    implementation(libs.antPattern)
    implementation(libs.kotlinGradle)

    testImplementation(libs.mockitoKotlin)
    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:gradle:git")))
    testImplementation(project(":subprojects:logger:logger"))

    testFixturesApi(project(":subprojects:common:result"))
    testFixturesApi(project(":subprojects:gradle:test-project"))
    testFixturesImplementation(libs.truth)
}

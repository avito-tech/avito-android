plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":gradle:module-dependencies-graph"))

    implementation(gradleApi())

    implementation(project(":gradle:android"))
    implementation(project(":logger:gradle-logger"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:process"))

    implementation(libs.antPattern)
    implementation(libs.kotlinPlugin)

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":gradle:git-test-fixtures"))
    testImplementation(project(":gradle:test-project"))
    testImplementation(testFixtures(project(":logger:logger")))

    testImplementation(libs.mockitoKotlin)
}

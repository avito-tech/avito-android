plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(project(":gradle:process"))
    api(project(":common:result"))

    implementation(gradleApi())
    implementation(project(":common:logger"))
    implementation(project(":common:slf4j-logger"))
    implementation(project(":gradle:gradle-extensions"))

    testImplementation(project(":gradle:test-project"))
    testImplementation(testFixtures(project(":common:logger")))
    testImplementation(libs.mockitoJUnitJupiter)
}

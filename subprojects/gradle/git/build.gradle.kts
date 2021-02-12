plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(project(":subprojects:gradle:build-environment")) {
        because("project.buildEnvironment only")
    }
    api(project(":subprojects:gradle:process"))

    implementation(gradleApi())
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:common:slf4j-logger"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(libs.funktionaleTry)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(libs.mockitoJUnitJupiter)
}

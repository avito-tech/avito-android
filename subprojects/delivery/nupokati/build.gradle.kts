plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.kotlin-serialization")
}

dependencies {
    implementation(libs.androidGradle)

    implementation(project(":subprojects:delivery:qapps"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:logger:gradle-logger"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:logger:slf4j-gradle-logger"))
    implementation(project(":subprojects:test-runner:instrumentation-tests"))
    implementation(project(":subprojects:test-runner:report-viewer"))

    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(project(":subprojects:common:resources"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(libs.jsonAssert)

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    gradleTestImplementation(project(":subprojects:common:test-okhttp"))
}

gradlePlugin {
    plugins {
        create("nupokati") {
            id = "com.avito.android.nupokati"
            implementationClass = "com.avito.android.NupokatiPlugin"
            displayName = "Nupokati service plugin"
        }
    }
}

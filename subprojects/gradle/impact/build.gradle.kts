plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    api(project(":subprojects:gradle:impact-shared"))

    implementation(gradleApi())
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:common:math"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:build-environment"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:statsd-config"))

    implementation(libs.antPattern)
    implementation(libs.kotlinGradle)

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    gradleTestImplementation(testFixtures(project(":subprojects:common:statsd")))
    gradleTestImplementation(testFixtures(project(":subprojects:gradle:build-environment")))
    gradleTestImplementation(testFixtures(project(":subprojects:gradle:impact-shared")))
    gradleTestImplementation(testFixtures(project(":subprojects:gradle:impact-shared")))
}

gradlePlugin {
    plugins {
        create("impact") {
            id = "com.avito.android.impact"
            implementationClass = "com.avito.impact.plugin.ImpactAnalysisPlugin"
            displayName = "Impact analysis"
        }
    }
}

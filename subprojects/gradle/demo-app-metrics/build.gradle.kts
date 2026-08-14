plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.kotlin-serialization")
    id("convention.test-fixtures")
    id("convention.gradle-testing")
}
dependencies {
    implementation(project(":subprojects:gradle:module-types-api"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:clickstream"))
    implementation(project(":subprojects:gradle:process"))
    implementation(libs.jdgraphtCore)
    implementation(libs.kotlinGradle)
    runtimeOnly(project(":subprojects:gradle:module-types")) {
        because("Need to run gradleTest. Adding to gradleTestRuntime doesn't work")
    }

    testImplementation(testFixtures(project(":subprojects:gradle:module-types")))
    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    testFixturesImplementation(testFixtures(project(":subprojects:gradle:module-types")))
    testFixturesImplementation(libs.kotlinx.serialization.json)
}

gradlePlugin {
    plugins {
        create("demo-app-metrics") {
            id = "com.avito.android.demo-app-metrics"
            implementationClass = "com.avito.android.DemoAppMetricsPlugin"
        }
    }
}

plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:build-failer"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:build-metrics"))
    implementation(project(":subprojects:gradle:sentry-config"))
    implementation(project(":subprojects:gradle:build-environment"))
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(Dependencies.Gradle.kotlinPlugin)
    implementation(Dependencies.funktionaleTry)

    // for test
    implementation(project(":subprojects:gradle:module-types"))
    implementation(project(":subprojects:gradle:room-config"))

    testImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("buildChecks") {
            id = "com.avito.android.buildchecks"
            implementationClass = "com.avito.android.plugin.build_param_check.BuildParamCheckPlugin"
            displayName = "Build checks"
        }
    }
}

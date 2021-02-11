plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.gson)

    testImplementation(project(":subprojects:common:logger-test-fixtures"))
    testImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("featureTogglesReport") {
            id = "com.avito.android.feature-toggles"
            implementationClass = "com.avito.android.plugin.FeatureTogglesPlugin"
            displayName = "Feature-toggle reporter"
        }
    }
}

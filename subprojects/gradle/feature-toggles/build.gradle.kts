plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:process"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:ci-logger"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.gson)

    testImplementation(project(":common:logger-test-fixtures"))
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

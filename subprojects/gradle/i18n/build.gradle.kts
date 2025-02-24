plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-extensions"))

    gradleTestImplementation(libs.truth)
    gradleTestImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("i18n") {
            id = "com.avito.android.i18n"
            implementationClass = "com.avito.i18n.plugin.TranslationPlugin"
            displayName = "Translation strings"
        }
    }
}

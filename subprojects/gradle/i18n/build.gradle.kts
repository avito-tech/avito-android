plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
    id("convention.kotlin-serialization")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:mtls"))

    implementation(libs.okhttp)
    implementation(libs.bundles.ktor)

    gradleTestImplementation(libs.truth)
    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    gradleTestImplementation(project(":subprojects:common:test-okhttp"))

    testImplementation(libs.xmlUnit.core)
    testImplementation(libs.xmlUnit.matchers)

    testFixturesImplementation(testFixtures(project(":subprojects:gradle:mtls")))
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

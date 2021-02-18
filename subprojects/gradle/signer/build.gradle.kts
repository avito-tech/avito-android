plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing-legacy")
}

dependencies {
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:build-failer"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:throwable-utils"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:common:files"))

    implementation(libs.funktionaleTry)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(testFixtures(project(":subprojects:common:logger")))
}

gradlePlugin {
    plugins {
        create("signer") {
            id = "com.avito.android.signer"
            implementationClass = "com.avito.plugin.SignServicePlugin"
            displayName = "Signer"
        }
    }
}

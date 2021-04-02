plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":common:files"))
    implementation(project(":common:okhttp"))
    implementation(project(":common:http-client"))
    implementation(project(":common:result"))
    implementation(project(":common:throwable-utils"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:build-failer"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:gradle-logger"))

    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.kotlinStdlib)

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(testFixtures(project(":common:logger")))
    testImplementation(testFixtures(project(":common:http-client")))

    gradleTestImplementation(project(":gradle:test-project"))
    gradleTestImplementation(project(":common:test-okhttp"))
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

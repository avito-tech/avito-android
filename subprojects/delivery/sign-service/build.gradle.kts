plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:worker-extensions"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:build-failer"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:logger:slf4j-gradle-logger"))

    implementation(libs.okhttp)

    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(project(":subprojects:common:test-okhttp"))

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    gradleTestImplementation(project(":subprojects:common:test-okhttp"))
}

gradlePlugin {
    plugins {
        create("signer") {
            id = "com.avito.android.sign-service"
            implementationClass = "com.avito.android.signer.SignServicePlugin"
            displayName = "Signer"
        }
    }
}

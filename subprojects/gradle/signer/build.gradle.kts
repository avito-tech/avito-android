plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:build-failer"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":common:okhttp"))
    implementation(project(":common:throwable-utils"))
    implementation(project(":gradle:android"))
    implementation(project(":common:files"))

    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.okhttp)
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:logger-test-fixtures"))
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

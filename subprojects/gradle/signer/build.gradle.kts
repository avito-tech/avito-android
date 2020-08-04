plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:ci-logger"))
    implementation(project(":common:okhttp"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:files"))

    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.gradle.androidPlugin)
    implementation(Dependencies.okhttp)
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":gradle:utils"))
    testCompile(Dependencies.test.okhttpMockWebServer)
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

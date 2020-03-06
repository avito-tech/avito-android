plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:files"))

    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.gradle.androidPlugin)
    implementation(Dependencies.okhttp)
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:utils"))
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

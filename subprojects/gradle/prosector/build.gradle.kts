plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:logging"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:kotlin-dsl-support"))

    implementation(Dependencies.gradle.androidPlugin)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":gradle:test-project"))
    testImplementation(Dependencies.test.okhttpMockWebServer)
}

gradlePlugin {
    plugins {
        create("prosector") {
            id = "com.avito.android.prosector"
            implementationClass = "ProsectorPlugin"
            displayName = "Prosector"
        }
    }
}

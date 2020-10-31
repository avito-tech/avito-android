plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:android"))
    implementation(project(":common:okhttp"))
    implementation(project(":gradle:build-failer"))
    implementation(project(":gradle:ci-logger"))
    implementation(project(":common:logger"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.Gradle.androidPlugin)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:test-okhttp"))
}

gradlePlugin {
    plugins {
        create("qapps") {
            id = "com.avito.android.qapps"
            implementationClass = "com.avito.plugin.QAppsPlugin"
            displayName = "QApps"
        }
    }
}

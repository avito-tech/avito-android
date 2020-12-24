plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:android"))
    implementation(project(":common:okhttp"))
    implementation(project(":gradle:build-failer"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":common:logger"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:logger-test-fixtures"))
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

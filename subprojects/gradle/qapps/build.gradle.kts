plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:build-failer"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:common:logger"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:common:logger-test-fixtures"))
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

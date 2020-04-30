plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:common:logger"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.gradle.androidPlugin)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(Dependencies.test.okhttpMockWebServer)
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

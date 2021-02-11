plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:common:okhttp"))

    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:common:test-okhttp"))
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

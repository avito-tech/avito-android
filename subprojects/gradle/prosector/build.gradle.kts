plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":common:okhttp"))

    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:test-okhttp"))
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

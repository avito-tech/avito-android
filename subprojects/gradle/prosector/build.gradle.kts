plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val androidGradlePluginVersion: String by project
val retrofitVersion: String by project
val okhttpVersion: String by project

dependencies {
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))

    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
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

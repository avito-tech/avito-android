plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val androidGradlePluginVersion: String by project
val retrofitVersion: String by project
val okhttpVersion: String by project

dependencies {
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":signer"))
    implementation(project(":android"))
    implementation(project(":okhttp"))
    implementation(project(":utils"))
    implementation(project(":git"))
    implementation(project(":logging"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")

    testImplementation(project(":test-project"))
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
}

gradlePlugin {
    plugins {
        create("qapps") {
            id = "com.avito.android.qapps"
            implementationClass = "com.avito.plugin.QAppsPlugin"
        }
    }
}

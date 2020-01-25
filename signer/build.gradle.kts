plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val funktionaleVersion: String by project
val androidGradlePluginVersion: String by project
val okhttpVersion: String by project

dependencies {
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":okhttp"))
    implementation(project(":android"))
    implementation(project(":files"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")

    testImplementation(project(":test-project"))
    testImplementation(project(":utils"))
    testCompile("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
}

gradlePlugin {
    plugins {
        create("signer") {
            id = "com.avito.android.signer"
            implementationClass = "com.avito.plugin.SignServicePlugin"
        }
    }
}

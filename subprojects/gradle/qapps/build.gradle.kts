plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
}

val funktionaleVersion: String by project
val androidGradlePluginVersion: String by project
val retrofitVersion: String by project
val okhttpVersion: String by project

dependencies {
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:signer"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:logging"))
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
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

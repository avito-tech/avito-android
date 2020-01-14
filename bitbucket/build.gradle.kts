plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val okhttpVersion: String by project
val retrofitVersion: String by project
val sentryVersion: String by project

dependencies {
    implementation(gradleApi())

    implementation(project(":utils"))
    implementation(project(":okhttp"))
    implementation(project(":git"))
    implementation(project(":impact"))
    implementation(project(":kotlin-dsl-support"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("io.sentry:sentry:$sentryVersion")

    testImplementation(project(":test-project"))
    testImplementation(project(":test-okhttp"))
}

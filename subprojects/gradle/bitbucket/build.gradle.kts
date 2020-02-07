plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val funktionaleVersion: String by project
val okhttpVersion: String by project
val retrofitVersion: String by project
val sentryVersion: String by project

dependencies {
    implementation(gradleApi())

    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:impact"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("io.sentry:sentry:$sentryVersion")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:common:test-okhttp"))
}

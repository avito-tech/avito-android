plugins {
    id("kotlin")
    `maven-publish`
}

val okhttpVersion: String by project
val gsonVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")

    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:git"))

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(testFixtures(project(":subprojects:gradle:git")))
}

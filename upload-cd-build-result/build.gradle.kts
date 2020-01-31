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

    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":android"))
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":git"))

    testImplementation(project(":test-project"))
    testImplementation(project(":test-okhttp"))
    testImplementation(testFixtures(project(":git")))
}

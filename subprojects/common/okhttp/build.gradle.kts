plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val funktionaleVersion: String by project
val okhttpVersion: String by project
val retrofitVersion: String by project

dependencies {
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation(project(":subprojects:common:logger"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation("org.funktionale:funktionale-try:$funktionaleVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    testImplementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    testImplementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
}

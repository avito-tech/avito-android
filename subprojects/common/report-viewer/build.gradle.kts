plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
    id("com.jfrog.bintray")
}

val gsonVersion: String by project
val kotsonVersion: String by project
val okhttpVersion: String by project
val jsonPathVersion: String by project
val funktionaleVersion: String by project

dependencies {
    implementation(project(":subprojects:common:okhttp"))
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("com.github.salomonbrys.kotson:kotson:$kotsonVersion")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")

    testImplementation(project(":subprojects:gradle:test-project")) //todo remove, we need to extract fileFromJarResources() to other module
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    testImplementation("com.jayway.jsonpath:json-path-assert:$jsonPathVersion")

    testFixturesImplementation(project(":subprojects:common:test-okhttp"))
    testFixturesImplementation("com.google.code.gson:gson:$gsonVersion")
    testFixturesImplementation("org.funktionale:funktionale-try:$funktionaleVersion")
}

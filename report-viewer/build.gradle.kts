plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val kotlinVersion: String by project
val gsonVersion: String by project
val kotsonVersion: String by project
val okhttpVersion: String by project
val teamcityRestClientVersion: String by project
val jsonPathVersion: String by project
val funktionaleVersion: String by project

dependencies {
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":teamcity"))
    implementation(project(":statsd"))
    implementation(project(":okhttp"))
    implementation(project(":kotlin-dsl-support"))

    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("com.github.salomonbrys.kotson:kotson:$kotsonVersion")

    compile("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    compile("org.funktionale:funktionale-try:$funktionaleVersion")
    compile("com.squareup.okhttp3:okhttp:$okhttpVersion")
    compile("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    compile("org.jetbrains.teamcity:teamcity-rest-client:$teamcityRestClientVersion")

    testImplementation(project(":test-project"))
    testImplementation(project(":test-okhttp"))

    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    testImplementation("com.jayway.jsonpath:json-path-assert:$jsonPathVersion")

    testFixturesImplementation(project(":test-okhttp"))
    testFixturesImplementation("org.funktionale:funktionale-try:$funktionaleVersion")
}

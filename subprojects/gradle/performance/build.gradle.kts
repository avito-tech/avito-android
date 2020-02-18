plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val teamcityRestClientVersion: String by project
val statsdVersion: String by project
val gsonVersion: String by project
val kotsonVersion: String by project
val okhttpVersion: String by project
val jslackVersion: String by project
val funktionaleVersion: String by project
val retrofitVersion: String by project
val mockitoKotlin2Version: String by project
val truthVersion: String by project

dependencies {
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:bitbucket"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:files"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:instrumentation-tests"))
    implementation(project(":subprojects:gradle:teamcity"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:git"))
    implementation("org.jetbrains.teamcity:teamcity-rest-client:$teamcityRestClientVersion")
    implementation("com.timgroup:java-statsd-client:$statsdVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("com.github.salomonbrys.kotson:kotson:$kotsonVersion")
    implementation("com.github.seratch:jslack-api-client:$jslackVersion") {
        exclude(group = "com.squareup.okhttp3")
    }
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:common:report-viewer")))
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlin2Version")
    testImplementation("com.google.truth:truth:$truthVersion")
}

gradlePlugin {
    plugins {
        create("com.avito.android.performance") {
            id = "com.avito.android.performance"
            implementationClass = "com.avito.performance.PerformancePlugin"
            displayName = "Performance testing"
        }
    }
}

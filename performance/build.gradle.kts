plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val teamcityRestClientVersion: String by project
val statsdVersion: String by project
val gsonVersion: String by project
val kotsonVersion: String by project
val jslackVersion: String by project
val retrofitVersion: String by project
val mockitoKotlin2Version: String by project
val truthVersion: String by project

dependencies {
    implementation(project(":report-viewer"))
    implementation(project(":android"))
    implementation(project(":bitbucket"))
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":utils"))
    implementation(project(":process"))
    implementation(project(":files"))
    implementation(project(":logging"))
    implementation(project(":instrumentation"))
    implementation(project(":teamcity"))
    implementation(project(":statsd"))
    implementation(project(":git"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.teamcity:teamcity-rest-client:$teamcityRestClientVersion")
    implementation("com.timgroup:java-statsd-client:$statsdVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("com.github.salomonbrys.kotson:kotson:$kotsonVersion")
    implementation("com.github.seratch:jslack:$jslackVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")

    testImplementation(project(":test-project"))
    testImplementation(testFixtures(project(":report-viewer")))
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlin2Version")
    testImplementation("com.google.truth:truth:$truthVersion")
}

gradlePlugin {
    plugins {
        create("com.avito.android.performance") {
            id = "com.avito.android.performance"
            implementationClass = "com.avito.performance.PerformancePlugin"
        }
    }
}

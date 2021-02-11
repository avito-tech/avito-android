plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.Test.okhttpMockWebServer)
    api(project(":subprojects:common:logger"))

    implementation(Dependencies.Test.truth)
    implementation(Dependencies.gson)
    implementation(Dependencies.commonsLang)

    implementation(project(":subprojects:common:junit-utils"))
    implementation(project(":subprojects:common:resources"))
    implementation(project(":subprojects:common:waiter"))

    testImplementation(testFixtures(project(":subprojects:common:logger")))
}

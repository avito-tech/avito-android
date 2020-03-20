plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:common:teamcity-common"))
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:utils"))

    testImplementation(project(":subprojects:gradle:test-project"))
}

plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val androidGradlePluginVersion: String by project
val funktionaleVersion: String by project

dependencies {
    api("com.android.tools.build:gradle:$androidGradlePluginVersion")

    implementation(gradleApi())
    implementation(project(":subprojects:gradle:files"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")

    testImplementation(testFixtures(project(":subprojects:gradle:process")))
}

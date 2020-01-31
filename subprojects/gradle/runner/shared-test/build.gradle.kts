plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinCoroutinesVersion: String by project
val funktionaleVersion: String by project
val junit5Version: String by project
val truthVersion: String by project

dependencies {
    compileOnly(gradleApi())
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.google.truth:truth:$truthVersion")
    implementation(project(":subprojects:gradle:runner:service"))
    implementation(project(":subprojects:gradle:runner:shared"))
    implementation(project(":subprojects:gradle:test-project"))
}

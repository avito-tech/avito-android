plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val kotlinCoroutinesVersion: String by project
val funktionaleVersion: String by project
val junit5Version: String by project
val truthVersion: String by project

dependencies {
    compileOnly(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.google.truth:truth:$truthVersion")
    implementation(project(":runner:service"))
    implementation(project(":runner:shared"))
    implementation(project(":test-project"))
}

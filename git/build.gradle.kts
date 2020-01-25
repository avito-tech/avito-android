plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val kotlinVersion: String by project
val funktionaleVersion: String by project
val mockitoJunit5Version: String by project

dependencies {
    implementation(gradleApi())
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":process"))
    implementation(project(":utils")) // project.buildEnvironment only
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")

    testImplementation(project(":test-project"))
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunit5Version")

    testFixturesImplementation(project(":test-project"))
    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}

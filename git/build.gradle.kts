plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val kotlinVersion: String by project
val mockitoJunit5Version: String by project

dependencies {
    implementation(gradleApi())
    implementation(project(":kotlin-dsl-support"))
    // can't use logging due to cyclic dependency
    implementation(project(":utils"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    testImplementation(project(":test-project"))
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunit5Version")

    testFixturesImplementation(project(":test-project"))
    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}

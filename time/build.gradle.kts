plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val kotlinVersion: String by project

dependencies {
    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}

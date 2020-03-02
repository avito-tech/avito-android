plugins {
    id("kotlin")
    `maven-publish`
    id("java-test-fixtures")
    id("com.jfrog.bintray")
}

val kotlinVersion: String by project

dependencies {
    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}

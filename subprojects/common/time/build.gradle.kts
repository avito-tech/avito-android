plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
    id("com.jfrog.bintray")
}

val kotlinVersion: String by project

dependencies {
    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}

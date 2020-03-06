plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    testFixturesImplementation(Dependencies.kotlinStdlib)
}

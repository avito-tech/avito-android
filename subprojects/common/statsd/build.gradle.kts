plugins {
    id("kotlin")
    `maven-publish`
    id("java-test-fixtures")
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.statsd)

    testFixturesImplementation(Dependencies.kotlinStdlib)
}

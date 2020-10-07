plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.commonsText)
    testImplementation(Dependencies.test.truth)
}

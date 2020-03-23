plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.test.junit)
    implementation(Dependencies.test.truth)
    implementation(Dependencies.test.hamcrestLib)

    testImplementation(Dependencies.kotlinReflect)
}

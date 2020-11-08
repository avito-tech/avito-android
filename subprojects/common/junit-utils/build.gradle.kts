plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.Test.junit)
    implementation(Dependencies.Test.truth)
    implementation(Dependencies.Test.hamcrestLib)

    testImplementation(Dependencies.kotlinReflect)
}

plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.Test.robolectric)
    implementation(Dependencies.Test.junit)
}

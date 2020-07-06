plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.proguardRetrace)

    testImplementation(Dependencies.test.junit)
}
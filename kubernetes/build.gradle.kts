plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project

dependencies {
    // TODO: describe reasons why don't we use an official client https://github.com/kubernetes-client/java
    api("io.fabric8:kubernetes-client:4.6.3")
    api("com.fkorotkov:kubernetes-dsl:1.2.1")
    // because kubernetes dsl uses old version
    api("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    implementation(gradleApi())
}

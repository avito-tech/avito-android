plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val rxjava1Version: String by project

dependencies {
    compileOnly(gradleApi())
    implementation("io.reactivex:rxjava:${rxjava1Version}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
}

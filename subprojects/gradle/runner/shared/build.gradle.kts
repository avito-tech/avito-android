plugins {
    id("kotlin")
    `maven-publish`
}

extra["artifact-id"] = "runner-shared"

val rxjava1Version: String by project

dependencies {
    compileOnly(gradleApi())
    implementation("io.reactivex:rxjava:$rxjava1Version")
}

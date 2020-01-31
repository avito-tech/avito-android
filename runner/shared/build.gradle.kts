plugins {
    id("kotlin")
    `maven-publish`
}

val rxjava1Version: String by project

dependencies {
    compileOnly(gradleApi())
    implementation("io.reactivex:rxjava:$rxjava1Version")
}

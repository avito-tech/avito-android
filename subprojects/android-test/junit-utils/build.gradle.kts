plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val kotlinVersion: String by project
val hamcrestVersion: String by project
val junitVersion: String by project
val truthVersion: String by project

dependencies {
    implementation("junit:junit:$junitVersion")
    implementation("com.google.truth:truth:$truthVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.hamcrest:hamcrest-library:$hamcrestVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
}

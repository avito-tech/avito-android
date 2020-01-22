plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val hamcrestVersion: String by project
val junitVersion: String by project
val truthVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    compile("junit:junit:$junitVersion")
    compile("com.google.truth:truth:$truthVersion")
    compile("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    compile("org.hamcrest:hamcrest-library:$hamcrestVersion")
    testImplementation(
        "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"
    )
}

plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val hamcrestVersion: String by project
val mockitoKotlinVersion: String by project
val mockitoCoreVersion: String by project

dependencies {
    implementation("org.hamcrest:hamcrest-library:$hamcrestVersion")
    compile("org.mockito:mockito-core:$mockitoCoreVersion")
    compile("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion") { exclude(group = "org.jetbrains.kotlin") }
    testImplementation(project(":subprojects:android-test:junit-utils"))
}

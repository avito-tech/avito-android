plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.test.hamcrestLib)
    compile(Dependencies.test.mockitoCore)
    compile(Dependencies.test.mockitoKotlin) { exclude(group = "org.jetbrains.kotlin") }
    testImplementation(project(":subprojects:android-test:junit-utils"))
}

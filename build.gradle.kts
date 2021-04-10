/**
 * Tests run from IDE in subprojects module can't recognize root wrapper
 */
val subprojectsWrapper by tasks.registering(Copy::class) {
    from("$rootDir/gradle/wrapper")
    into("$rootDir/subprojects/gradle/wrapper")
}

tasks.withType<Wrapper> {
    // sources unavailable with BIN until https://youtrack.jetbrains.com/issue/IDEA-231667 resolved
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "7.0"

    finalizedBy(subprojectsWrapper)
}

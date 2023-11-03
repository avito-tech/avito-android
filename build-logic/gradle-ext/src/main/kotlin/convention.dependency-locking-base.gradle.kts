plugins {
    base
    id("com.dropbox.dependency-guard")
}

tasks.named("check").configure {
    dependsOn(tasks.dependencyGuard)
}

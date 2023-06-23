plugins {
    id("convention.dependency-locking-base")
}

dependencyGuard {
    configuration("runtimeClasspath")
}

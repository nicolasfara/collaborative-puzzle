plugins {
    base
    java
    idea
    application
    kotlin("jvm") version "1.3.72" apply false
    id("io.gitlab.arturbosch.detekt").version("1.10.0-RC1")
}

subprojects {
    group = "it.unibo.pcd.collaborativepuzzle"
    version = "1.0"
    repositories {
        jcenter()
    }
}


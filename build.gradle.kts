plugins {
    base
    idea
    kotlin("jvm") version "1.3.72" apply false
    id("io.gitlab.arturbosch.detekt").version("1.10.0-RC1")
}

allprojects {
    group = "it.unibo.pcd.collaborativepuzzle"
    version = "1.0"
    repositories {
        jcenter()
    }
}

dependencies {
    // Make the root project archives configuration depend on every subproject
    subprojects.forEach {
        archives(it)
    }
}

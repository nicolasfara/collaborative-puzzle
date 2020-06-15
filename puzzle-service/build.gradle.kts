plugins {
    kotlin("jvm")
}

group = "it.unibo.pcd.collaborativepuzzle"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.vertx:vertx-core:3.9.1")
    implementation("io.vertx:vertx-lang-kotlin:3.9.1")
    implementation("io.vertx:vertx-web:3.9.1")
    implementation("io.vertx:vertx-rabbitmq-client:3.9.1")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:slf4j-simple:1.7.30")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
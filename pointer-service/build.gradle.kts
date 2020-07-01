plugins {
    java
    kotlin("jvm")
}

group = "it.unibo.pcd.collaborativepuzzle"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    with("io.vertx") {
        implementation("$this:vertx-core:3.9.1")
        implementation("$this:vertx-lang-kotlin:3.9.1")
        implementation("$this:vertx-web:3.9.1")
        implementation("$this:vertx-rabbitmq-client:3.9.1")
        implementation("$this:vertx-lang-kotlin-coroutines:3.9.1")
        implementation("$this:vertx-mongo-client:3.9.1")
    }

    with("org.slf4j") {
        implementation("$this:slf4j-api:1.7.30")
        implementation("$this:slf4j-simple:1.7.30")
    }

    implementation("io.github.cdimascio:java-dotenv:5.2.1")

    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
tasks.withType<Jar> {
    // Otherwise you'll get a "No main manifest attribute" error
    manifest {
        attributes["Main-Class"] = "it.unibo.pcd.pointerservice.PointerServiceKt"
    }

    // To add all of the dependencies otherwise a "NoClassDefFoundError" error
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

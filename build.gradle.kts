plugins {
    java
    kotlin("jvm") version "1.9.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.andjsrk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}

application {
    mainClass.set("$group.$name.MainKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    jar {
        dependsOn(shadowJar)
    }
    test {
        useJUnitPlatform()
    }
    withType<Test> {
        environment("TEST", true)
    }
}

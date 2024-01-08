plugins {
    java
    kotlin("jvm") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.andjsrk"
version = "1.0-SNAPSHOT"

allprojects {
    apply(plugin="org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib"))
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}

subprojects {
    apply(plugin="com.github.johnrengelman.shadow")

    group = rootProject.group
    version = rootProject.version

    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    }
}

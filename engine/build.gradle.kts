dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
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

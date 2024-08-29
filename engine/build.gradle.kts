dependencies {
    testImplementation(kotlin("test"))
}

tasks {
    test {
        useJUnitPlatform()
    }
    withType<Test> {
        environment("TEST", true)
    }
}

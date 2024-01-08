dependencies {
    testImplementation(kotlin("test"))
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

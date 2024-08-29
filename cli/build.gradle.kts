plugins {
    application
}

application {
    mainClass.set("$group.${rootProject.name}.$name.MainKt")
}

dependencies {
    implementation(project(":engine"))
}

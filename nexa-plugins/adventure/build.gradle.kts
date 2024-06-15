plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nexa-core"))

    api(project(":nexa-plugins:nexa-plugin-profile"))
}

dependencies {
    testApi(kotlin("test"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components.getByName("kotlin"))
        artifact(tasks.kotlinSourcesJar)
    }
}

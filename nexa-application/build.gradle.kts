plugins {
    kotlin("jvm")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

dependencies {
    api(project(":nexa-core"))

    api(project(":nexa-plugins:nexa-plugin-adapter-discord"))
    api(project(":nexa-plugins:nexa-plugin-profile"))
    api(project(":nexa-plugins:nexa-plugin-help"))
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

application {
    mainClass.set("pixel.nexa.application.NexaEngineBootstrap")
}

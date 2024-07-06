import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("com.github.johnrengelman.shadow")
    application
}

dependencies {
    api(project(":nexa-core"))
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

tasks.withType<ShadowJar> {
    enabled = true
}

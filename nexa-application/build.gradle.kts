plugins {
    kotlin("jvm")
    id("maven-publish")
    application
}

dependencies {
    api(project(":nexa-core"))

    implementation(project(":nexa-plugins:nexa-plugin-adapter-discord"))
    implementation(project(":nexa-plugins:nexa-plugin-profile"))
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

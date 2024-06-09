plugins {
    kotlin("jvm")
    id("maven-publish")

}

dependencies {
    api(project(":nexa-core"))

    api("net.dv8tion:JDA:5.0.0-beta.24")
    api("club.minnced:jda-ktx:0.11.0-beta.20")
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

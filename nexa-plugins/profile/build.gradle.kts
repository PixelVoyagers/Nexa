plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":nexa-core"))

    implementation("com.google.zxing:core:3.5.3")
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

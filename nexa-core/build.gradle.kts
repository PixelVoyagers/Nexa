plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api("com.github.PixelVoyagers.AuxFramework:aux-core:${property("aux.version")}")
    api("com.github.PixelVoyagers.AuxFramework:aux-application:${property("aux.version")}")
    api("com.github.PixelVoyagers.AuxFramework:aux-web:${property("aux.version")}")
    api("com.github.PixelVoyagers.Aurora:aurora-common:983fb33154")

    api("de.undercouch:bson4jackson:2.15.1")
    api("org.springframework:spring-core:6.1.8")

    api("org.jsoup:jsoup:1.17.2")
    api("com.microsoft.playwright:playwright:1.44.0")
    api("org.thymeleaf:thymeleaf:3.1.2.RELEASE")

    api("io.ktor:ktor-server-websockets:3.0.0-beta-1")
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

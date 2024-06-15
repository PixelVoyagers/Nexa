plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api("com.github.PixelVoyagers.AuxFramework:aux-core:${property("aux.version")}")
    api("com.github.PixelVoyagers.AuxFramework:aux-application:${property("aux.version")}")
    api("com.github.PixelVoyagers.AuxFramework:aux-web:${property("aux.version")}")
    api("com.github.PixelVoyagers.Aurora:aurora-compiler:bb159b5a59")

    api("de.undercouch:bson4jackson:2.15.1")
    api("org.springframework:spring-core:6.1.8")

    api("org.jsoup:jsoup:1.17.2")
    api("com.microsoft.playwright:playwright:1.44.0")
    api("org.thymeleaf:thymeleaf:3.1.2.RELEASE")
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

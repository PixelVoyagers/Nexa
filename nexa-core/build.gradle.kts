plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api("com.github.PixelVoyagers.AuxFramework:aux-core:${property("aux.version")}")
    api("com.github.PixelVoyagers.AuxFramework:aux-application:${property("aux.version")}")
    api("com.github.PixelVoyagers.AuxFramework:aux-web:${property("aux.version")}")

    api("org.springframework:spring-core:6.1.8")

    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.microsoft.playwright:playwright:1.44.0")
    implementation("org.thymeleaf:thymeleaf:3.1.2.RELEASE")
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

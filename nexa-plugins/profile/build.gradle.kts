import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    compileOnly(project(":nexa-core"))

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

tasks.withType<ShadowJar> {
    enabled = true
    exclude("kotlin/**", "org/intellij/**", "org/jetbrains/**")
    exclude("META-INF/kotlin-stdlib**.kotlin_module")
}


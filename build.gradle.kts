import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
    id("maven-publish")
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            name = "JitPack"
            url = uri("https://jitpack.io")
        }
    }

    tasks.withType<KotlinCompile> {
        kotlin {
            jvmToolchain(21)
            compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
        }
    }

    tasks.withType<Jar> {
        manifest {
            attributes("Implementation-Version" to project.version)
        }
    }
}

group = "pixel.nexa"
version = "1.0.0"

subprojects {
    group = rootProject.group
    version = rootProject.version
}

dependencies {
    subprojects.filter { it.name.startsWith("nexa-") }.forEach { api(it) }
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

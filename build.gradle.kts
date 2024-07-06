import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
version = "1.0.1"

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

allprojects {
    tasks.withType<ProcessResources> {
        val resourceTargets = listOf("plugin.yml")
        val replaceProperties = mapOf(
            Pair(
                "gradle",
                mapOf(
                    Pair("gradle", gradle),
                    Pair("project", this@allprojects)
                )
            )
        )
        filesMatching(resourceTargets) {
            expand(replaceProperties)
        }
    }
}

tasks.withType<ShadowJar> {
    enabled = false
}

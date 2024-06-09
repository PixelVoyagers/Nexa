plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "nexa-bot"

for (directory in rootProject.projectDir.listFiles()!!) {
    if (!directory.isDirectory) continue
    if (!directory.name.startsWith("nexa-")) continue
    if (directory.listFiles()?.firstOrNull { it.name == "build.gradle.kts" || it.name == "build.gradle" } != null) {
        include(directory.name)
    }
}

fun processSubprojects(projectName: String, nameMapping: (String) -> String) {
    for (file in findProject(projectName)!!.buildFile.parentFile.listFiles()!!) {
        if (!file.isDirectory) continue
        if (file.listFiles()?.firstOrNull { it.name == "build.gradle.kts" } != null) {
            include("$projectName:${file.name}")
            findProject("$projectName:${file.name}")?.let {
                it.name = nameMapping(file.name)
            }
        }
    }
}

processSubprojects(":nexa-plugins") { "nexa-plugin-$it" }

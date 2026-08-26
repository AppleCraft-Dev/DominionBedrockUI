plugins {
    `java-library`
}

group = "cn.lunadeer.dominion"
version = "1.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    // GeyserMC / floodgate
    maven("https://repo.opencollab.dev/main/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    // Dominion API
    compileOnly("cn.lunadeer:DominionAPI:4.9.6")

    // floodgate API
    compileOnly("org.geysermc.floodgate:api:2.2.5-SNAPSHOT")
}

tasks.processResources {
    val props = mapOf(
        "version" to version.toString(),
        "name" to rootProject.name
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

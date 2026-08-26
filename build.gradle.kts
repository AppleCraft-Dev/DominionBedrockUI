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

    // Dominion API（运行时由服务器上的 Dominion 插件提供）：
    // 方式一：-PdominionJar=<jar 路径> 显式指定；
    // 方式二：把 Dominion 插件 jar（内含 API 类，可从 Hangar / Modrinth 下载）
    //        或 DominionAPI jar 放入项目根目录 libs/ 下。
    val dominionJar = providers.gradleProperty("dominionJar").orNull
    if (dominionJar != null) {
        compileOnly(files(dominionJar))
    } else {
        compileOnly(fileTree("libs") { include("*.jar") })
    }

    // floodgate API（运行时由服务器上的 floodgate 插件提供）
    compileOnly("org.geysermc.floodgate:api:2.2.4-SNAPSHOT")
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

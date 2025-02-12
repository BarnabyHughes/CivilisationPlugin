plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.lucko.me/") // LuckPerms API repository
    maven("https://repo.oraxen.com/releases")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // LuckPerms API
    compileOnly("net.luckperms:api:5.4")

    compileOnly("io.th0rgal:oraxen:1.189.0")

    // Corrected Spigot API reference
    compileOnly(files("libs/spigot-api-1.21.1-R0.1-SNAPSHOT-shaded.jar"))
}

tasks.test {
    useJUnitPlatform()
}

// Set Java version
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17)) // Ensure compatibility with Minecraft versions
}

// Automatically copy the built JAR to IntelliJ project directory: /server/plugins/
tasks.register<Copy>("copyToIntelliJServer") {
    dependsOn(tasks.build)
    from(layout.buildDirectory.file("libs/${project.name}-${project.version}.jar"))
    into(file("${rootProject.rootDir}/server/plugins/")) // Target IntelliJ's directory
}

// Ensure the JAR is copied after build
tasks.build {
    finalizedBy("copyToIntelliJServer")
}

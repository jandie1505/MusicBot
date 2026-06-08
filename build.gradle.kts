plugins {
    id("java")
    id("application")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.lavalink.dev/releases")
}

dependencies {
    implementation("net.dv8tion:JDA:6.3.0")
    implementation("commons-io:commons-io:2.21.0")
    implementation("org.xerial:sqlite-jdbc:3.51.1.0")
    implementation("ch.qos.logback:logback-classic:1.5.26")
    implementation("org.json:json:20251224")
    implementation("dev.arbjerg:lavaplayer:2.2.6")
    implementation("dev.lavalink.youtube:v2:1.17.0")
    implementation("org.apache.commons:commons-lang3:3.20.0")
    implementation("se.michaelthelin.spotify:spotify-web-api-java:8.5.0")
    implementation("org.jline:jline:3.30.6")
}

application {
    mainClass.set("net.jandie1505.musicbot.MusicBot")
}

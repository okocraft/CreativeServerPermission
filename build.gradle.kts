plugins {
    java
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))

group = "net.okocraft"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.112-stable")
    compileOnly("com.github.plan-player-analytics:Plan:5.8.3605")
    compileOnly("net.luckperms:api:5.5")
}

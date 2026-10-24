plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.23" apply false
    id("com.gradleup.shadow") version "9.6.1" apply false
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
}
plugins {
    id("java-library")
    id("io.papermc.paperweight.userdev")
    id("com.gradleup.shadow")
}

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("26.2.build.+")
    compileOnly(project(":api"))
    compileOnly(project(":nms"))
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}
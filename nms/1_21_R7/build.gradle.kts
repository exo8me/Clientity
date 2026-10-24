plugins {
    java
    id("io.papermc.paperweight.userdev")
    id("com.gradleup.shadow")
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
    compileOnly(project(":nms"))
}
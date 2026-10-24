plugins {
    java
    id("com.gradleup.shadow") version "9.4.2"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    implementation(project(":api"))
    implementation(project(":nms"))
    implementation(project(":nms:1_21_R7"))
    implementation(project(":nms:26_R2"))
}

tasks.shadowJar {
    archiveBaseName.set("Clientity")
    archiveClassifier.set("")
    archiveVersion.set("")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
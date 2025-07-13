plugins {
    id("java")
    id("com.gradleup.shadow") version ("8.3.4")
}

group = "me.meiallu.library"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains:annotations:26.0.1")
    implementation("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}
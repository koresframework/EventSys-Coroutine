import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.hierynomus.license") version "0.15.0"
    kotlin("jvm") version "1.4.32"
    id("java")
    application
}

group = "com.github.jonathanxd"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_9
    targetCompatibility = JavaVersion.VERSION_1_9
}

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
}

dependencies {
    // Kores
    implementation("com.github.koresframework.Kores:Kores:4.0.1.base")
    implementation("com.github.koresframework.Kores-SourceWriter:Kores-SourceWriter:4.0.1.source")
    implementation("com.github.koresframework:Kores-Extra:1.4.4")
    implementation("com.github.koresframework:KoresProxy:2.6.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    implementation("com.github.koresframework:EventSys:1.9.1")
    implementation("com.github.JonathanxD.JwIUtils:JwIUtils:4.17.2")
    implementation("com.github.JonathanxD.JwIUtils:jwiutils-kt:4.17.2")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "9"
}

tasks {
    withType<nl.javadude.gradle.plugins.license.License> {
        header = rootProject.file("LICENSE")
        strictCheck = true
    }
}
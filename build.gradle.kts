import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20-RC2"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "app.pwdr"
version = "1.0.0"
//java.sourceCompatibility = JavaVersion.VERSION_11
//java.targetCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))

    // NOTE: Required for Function primitives
    compileOnly("com.google.cloud.functions:functions-framework-api:1.0.4")

    // Gson
    implementation("com.google.code.gson:gson:2.9.0")

    // Test
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("com.google.guava:guava-testlib:31.1-jre")
    testImplementation("io.cloudevents:cloudevents-core:2.3.0")
    testImplementation("com.google.cloud.functions:functions-framework-api:1.0.4")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("functions-app")
        mergeServiceFiles()
    }
}

// REF: https://github.com/GoogleCloudPlatform/functions-framework-java#running-a-function-with-gradle

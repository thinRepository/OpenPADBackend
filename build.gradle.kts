/**
 * Build configuration for OpenPAD Backend.
 *
 * Kotlin-for-Java-devs notes:
 *  - `build.gradle.kts` is the Kotlin DSL equivalent of `build.gradle` (Groovy DSL).
 *    Everything is statically typed, so your IDE gives full autocompletion.
 *  - Plugin versions are declared once here; transitive dependency versions are managed
 *    by the Spring Boot dependency-management BOM, just like importing the Spring BOM
 *    in a Maven pom.xml.
 */

plugins {
    kotlin("jvm") version "1.9.25"
    // Adds `open` to Spring-proxied classes/methods automatically —
    // needed because Kotlin classes are `final` by default, unlike Java.
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.3"
    // Imports the Spring Boot BOM so you can omit versions on Spring deps below.
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.openpad"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // ── Core ──────────────────────────────────────────────────────────────────
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Provides Kotlin-specific Jackson serialization (data classes, null safety, etc.)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Required for Spring to use Kotlin reflection (e.g. constructor injection)
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // ── Testing ───────────────────────────────────────────────────────────────
    // Brings in JUnit 5, Mockito, AssertJ, Spring test utilities
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Kotlin-idiomatic Mockito wrappers: `mock<MyClass>()`, `whenever(...)`, etc.
    // This is the official Kotlin extension for Mockito — preferred over raw Mockito in Kotlin.
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

kotlin {
    compilerOptions {
        // Enables null safety checks for Java method return types.
        // Without this, platform types (T!) are unchecked — risky in Kotlin code.
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    // Use JUnit Platform (JUnit 5) runner
    useJUnitPlatform()
}

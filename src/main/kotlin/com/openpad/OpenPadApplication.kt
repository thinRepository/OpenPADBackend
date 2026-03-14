package com.openpad

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Entry point for the OpenPAD backend service.
 *
 * Kotlin-for-Java-devs notes:
 *  - `@SpringBootApplication` works identically to Java.
 *  - `runApplication<T>(*args)` is the Kotlin idiomatic replacement for
 *    `SpringApplication.run(T::class.java, *args)`. It's a reified inline
 *    function, so no `.class` reference is needed.
 *  - The `main` function lives at the top level (no wrapping class required),
 *    but the JVM still gets a synthetic class with a `main(String[])` method.
 */
@SpringBootApplication
class OpenPadApplication

fun main(args: Array<String>) {
    runApplication<OpenPadApplication>(*args)
}

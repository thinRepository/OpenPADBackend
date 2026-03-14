package com.openpad.service

import org.springframework.stereotype.Service

/**
 * Service encapsulating greeting business logic.
 *
 * **Why a separate service for something so simple?**
 * This establishes the Controller → Service layering that will grow naturally:
 *  - When a database is added, inject a `HelloRepository` here and delegate to it.
 *  - The [HelloController] never needs to change — it stays a thin HTTP adapter.
 *  - This class can be unit-tested without starting any web layer.
 *
 * Kotlin-for-Java-devs notes:
 *  - `@Service` is identical to Java; Spring component scanning picks it up.
 *  - A class with no constructor parameters doesn't need an explicit constructor.
 *  - `fun greet(): String = "Hello, World!"` is a single-expression function —
 *    equivalent to `public String greet() { return "Hello, World!"; }` in Java.
 */
@Service
class HelloService {

    /**
     * Returns the canonical greeting string.
     *
     * This operation is **pure** (no side-effects, no external dependencies),
     * which makes it trivially idempotent and easy to unit test.
     *
     * @return the greeting message
     */
    fun greet(): String = "Hello, World!"
}

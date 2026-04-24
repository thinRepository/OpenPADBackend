package com.openpad.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * Challenge entity representing a single challenge session.
 *
 * **Database-agnostic design:**
 *  - Uses JPA annotations (works with H2, PostgreSQL, MSSQL, etc.)
 *  - Spring Data JPA handles dialect differences automatically.
 *  - No hard-coded SQL or database-specific code.
 *
 * **Fields:**
 *  - [id]: Auto-generated unique identifier
 *  - [sha]: SHA256 hash unique to this challenge session
 *  - [createdAt]: Timestamp when the challenge was created
 *  - [expiresAt]: Timestamp when the challenge expires (TTL)
 *
 * Kotlin-for-Java-devs notes:
 *  - `@Entity` marks this as a JPA entity (identical to Java).
 *  - `data class` generates equals(), hashCode(), toString(), copy() automatically.
 *  - `val` makes all properties immutable (best practice for entities).
 *  - `Instant` is the modern Java/Kotlin temporal type (replaces Date).
 */
@Entity
@Table(name = "challenges")
data class Challenge(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 64)
    val sha: String,

    @Column(nullable = false)
    val createdAt: Instant,

    @Column(nullable = false)
    val expiresAt: Instant
)

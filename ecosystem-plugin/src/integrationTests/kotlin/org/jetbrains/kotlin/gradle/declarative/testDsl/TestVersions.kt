package org.jetbrains.kotlin.gradle.declarative.testDsl

interface TestVersions {
    object Gradle {
        const val G_9_4 = "9.4.1"

        const val MIN_SUPPORTED = G_9_4
        const val MAX_SUPPORTED = G_9_4
    }

    object Kotlin {
        const val CURRENT = "2.3.20"
    }
}
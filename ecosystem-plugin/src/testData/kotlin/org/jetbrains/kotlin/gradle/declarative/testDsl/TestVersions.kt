package org.jetbrains.kotlin.gradle.declarative.testDsl

interface TestVersions {
    object Gradle {
        const val G_9_8 = "9.8.0"

        const val MIN_SUPPORTED = G_9_8
        const val MAX_SUPPORTED = G_9_8
    }

    object Kotlin {
        const val CURRENT = "2.4.20"
    }

    object Dependencies {
        const val COROUTINES = "1.10.2"
        const val DATETIME = "0.7.1"
    }
}

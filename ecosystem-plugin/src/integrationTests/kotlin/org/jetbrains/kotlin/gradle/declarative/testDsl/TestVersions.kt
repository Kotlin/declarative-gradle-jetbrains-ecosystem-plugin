package org.jetbrains.kotlin.gradle.declarative.testDsl

interface TestVersions {
    object Gradle {
        const val G_9_7 = "9.7.1"

        const val MIN_SUPPORTED = G_9_7
        const val MAX_SUPPORTED = G_9_7
    }

    object Kotlin {
        const val CURRENT = "2.4.10"
    }

    object Dependencies {
        const val COROUTINES = "1.10.2"
        const val DATETIME = "0.7.1"
    }
}

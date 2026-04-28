package org.jetbrains.kotlin.gradle.declarative.testDsl

interface TestVersions {
    object Gradle {
        const val G_9_6 = "9.6.0-milestone-1"

        const val MIN_SUPPORTED = G_9_6
        const val MAX_SUPPORTED = G_9_6
    }

    object Kotlin {
        const val CURRENT = "2.3.21"
    }

    object Dependencies {
        const val COROUTINES = "1.10.2"
        const val DATETIME = "0.7.1"
    }
}
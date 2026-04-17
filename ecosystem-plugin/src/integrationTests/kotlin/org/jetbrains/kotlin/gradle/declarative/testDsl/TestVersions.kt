package org.jetbrains.kotlin.gradle.declarative.testDsl

interface TestVersions {
    object Gradle {
        const val G_9_5 = "9.5.0-rc-2"

        const val MIN_SUPPORTED = G_9_5
        const val MAX_SUPPORTED = G_9_5
    }

    object Kotlin {
        const val CURRENT = "2.3.20"
    }

    object Dependencies {
        const val COROUTINES = "1.10.2"
        const val DATETIME = "0.7.1"
    }
}
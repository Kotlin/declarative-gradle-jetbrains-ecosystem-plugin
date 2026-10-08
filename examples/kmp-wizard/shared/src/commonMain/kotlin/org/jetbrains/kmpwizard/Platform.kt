package org.jetbrains.kmpwizard

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
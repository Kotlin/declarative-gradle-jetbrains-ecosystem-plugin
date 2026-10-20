package org.jetbrains.ecosystem

import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.xdcl.Reaction
import org.gradle.api.xdcl.ReactionScope

class JvmApplicationReaction : Reaction<JvmApplication, Project> {

    override fun on(data: JvmApplication, context: Project, scope: ReactionScope) {
        context.pluginManager.apply("org.jetbrains.kotlin.jvm")
        context.pluginManager.apply("application")
        context.extensions.getByType(JavaApplication::class.java).mainClass.set(data.mainClass())
    }
}

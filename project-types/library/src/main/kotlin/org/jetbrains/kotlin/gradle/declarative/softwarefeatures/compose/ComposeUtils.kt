package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.compose

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

internal inline fun <reified T : Any> Project.propertyFromNullableVar(
    getter: Provider<T>,
    crossinline setter: context(Project) (T?) -> Unit
): Property<T> {
    val delegate = objects.property(T::class.java).convention(getter)
    return object : Property<T> by delegate {

        override fun set(provider: Provider<out T>) =
            delegate.set(provider.map { setter(it); it })

        override fun set(value: T?) =
            delegate.set(value).apply { setter(value) }

        override fun value(provider: Provider<out T>): Property<T> =
            delegate.value(provider.map { setter(it); it })

        override fun value(value: T?): Property<T> =
            delegate.value(value).apply { setter(value) }
    }
}

internal inline fun <reified T : Any> Project.propertyFromVar(
    getter: Provider<T>,
    crossinline setter: Project.(T) -> Unit
): Property<T> = propertyFromNullableVar(
    getter = getter,
    setter = { it?.let { setter(it) } ?: Unit },
)
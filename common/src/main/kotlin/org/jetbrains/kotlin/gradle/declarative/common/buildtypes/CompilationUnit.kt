package org.jetbrains.kotlin.gradle.declarative.common.buildtypes

import org.gradle.api.Named
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet

public interface CompilationUnit : Named {
    public val sources: SourceDirectorySet

    public val destinationDirectory: DirectoryProperty
    public val outputs: FileCollection
}
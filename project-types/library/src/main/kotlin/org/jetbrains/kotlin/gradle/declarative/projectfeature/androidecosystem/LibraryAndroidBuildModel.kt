package org.jetbrains.kotlin.gradle.declarative.projectfeature.androidecosystem

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.dsl.LibraryAndroidResources
import org.gradle.api.Project
import org.gradle.features.binding.BuildModel
import javax.inject.Inject

@Suppress("UnstableApiUsage")
public interface LibraryAndroidBuildModel : BuildModel {
    public val androidResources: LibraryAndroidResources
}

internal abstract class DefaultLibraryAndroidBuildModel @Inject constructor(project: Project) : LibraryAndroidBuildModel {

    override val androidResources: LibraryAndroidResources by lazy {
        project.extensions.getByType(KotlinMultiplatformAndroidLibraryExtension::class.java).androidResources
    }
}
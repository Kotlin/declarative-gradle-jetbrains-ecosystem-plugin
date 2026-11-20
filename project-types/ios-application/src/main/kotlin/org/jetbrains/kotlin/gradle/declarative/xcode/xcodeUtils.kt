@file:Suppress("INVISIBLE_REFERENCE")
package org.jetbrains.kotlin.gradle.declarative.xcode

import com.apple.xcode.project.format.XCSchema
import com.apple.xcode.project.format.XcodeProjectFormat.*
import org.jetbrains.kotlin.gradle.declarative.IosApplicationBuildModel
import org.jetbrains.kotlin.gradle.declarative.XcodeProjectGenerationExtension
import org.swift.swiftkit.core.collections.SwiftDictionaryMap
import org.swift.swiftkit.core.collections.SwiftSet
import java.util.Optional

private inline val <T : Any> T?.optional: Optional<T> get() = Optional.ofNullable(this)

private val nullObjectId: Optional<XCSchema.ObjectID> = Optional.empty()

private class MutableSwiftSet<T>(
    var delegate: SwiftSet<T>,
    val insertCallback: (SwiftSet<T>, T) -> SwiftSet<T>,
    val removeCallback: (SwiftSet<T>, T) -> SwiftSet<T>
) : MutableSet<T> {
    override fun iterator(): MutableIterator<T> = delegate.iterator()

    override fun add(element: T): Boolean {
        val result = element !in this
        delegate = insertCallback(delegate, element)
        return result
    }

    override fun remove(element: T): Boolean {
        val result = element in this
        delegate = removeCallback(delegate, element)
        return result
    }

    override fun addAll(elements: Collection<T>): Boolean = elements.fold(false) { modified, element ->
        val added = add(element)
        modified || added
    }

    override fun removeAll(elements: Collection<T>): Boolean = elements.fold(false) { modified, element ->
        val removed = remove(element)
        modified || removed
    }

    override fun retainAll(elements: Collection<T>): Boolean = TODO()

    override fun clear() = delegate.clear()

    override val size: Int
        get() = delegate.size

    override fun isEmpty(): Boolean = delegate.isEmpty()

    override fun contains(element: T): Boolean = element in delegate

    override fun containsAll(elements: Collection<T>): Boolean = elements.all { it in delegate }
}

private inline fun <T> Set<T>.asSwiftSet(emptySwiftSet: () -> SwiftSet<T>, insertCallback: (SwiftSet<T>, T) -> SwiftSet<T>): SwiftSet<T> =
    fold(emptySwiftSet()) { acc, element -> insertCallback(acc, element) }

private inline fun <K, V> Map<K, V>.asSwiftDictionary(emptySwiftMap: () -> SwiftDictionaryMap<K, V>, putIntoCallback: (SwiftDictionaryMap<K, V>, K, V) -> SwiftDictionaryMap<K, V>): SwiftDictionaryMap<K, V> =
    entries.fold(emptySwiftMap()) { acc, [key, value] -> putIntoCallback(acc, key, value) }

private fun Project(
    objectID: XCSchema.ObjectID? = null,
    rootGroupDebugID: XCSchema.ObjectID? = null,
    configurationListDebugID: XCSchema.ObjectID? = null,
    topLevelReferences: List<XCSchema.Reference>,
    packages: List<XCSchema.SwiftPackage> = emptyList(),
    configurations: List<XCSchema.Configuration>,
    buildSettings: Map<String, XCSchema.BuildSetting> = emptyMap(),
    defaultConfigurationName: XCSchema.ConfigurationName,
    targets: List<XCSchema.Target>,
    localizationInfo: XCSchema.ProjectLocalizationInfo,
    requiredCapabilities: Set<XCSchema.Capability>,
    buildIndependentTargetsInParallel: Boolean = false,
    lastUpgradeCheck: XCSchema.MarketingVersion? = null,
    lastSwiftUpdateCheck: XCSchema.MarketingVersion? = null,
    lastSwiftMigration: XCSchema.MarketingVersion? = null,
    organizationName: String? = null,
    classPrefix: String? = null,
    productsGroup: XCSchema.GroupTreeReference? = null,
    importedProducts: List<XCSchema.RemoteProduct> = emptyList()
) : XCSchema.Project = XCSchema.Project.init(
    objectID.optional,
    rootGroupDebugID.optional,
    configurationListDebugID.optional,
    topLevelReferences.toTypedArray(),
    packages.toTypedArray(),
    configurations.toTypedArray(),
    buildSettings.asSwiftDictionary(::emptyStringToBuildSettingDictionary, ::putIntoStringToBuildSettingDictionary),
    defaultConfigurationName,
    targets.toTypedArray(),
    localizationInfo,
    requiredCapabilities.asSwiftSet(::emptyCapabilitySet, ::insertIntoCapabilitySet),
    buildIndependentTargetsInParallel,
    lastUpgradeCheck.optional,
    lastSwiftUpdateCheck.optional,
    lastSwiftMigration.optional,
    organizationName.optional,
    classPrefix.optional,
    productsGroup.optional,
    importedProducts.toTypedArray()
)

private inline val String.asConfigurationName: XCSchema.ConfigurationName get() = XCSchema.ConfigurationName.initName(this)

private inline val String.asLanguage: XCSchema.Language get() = XCSchema.Language.initLanguageID(this)


private fun Configuration(
    name: XCSchema.ConfigurationName,
    file: XCSchema.GroupTreeAnchoredReference? = null,
    objectID: XCSchema.ObjectID? = null,
) : XCSchema.Configuration = XCSchema.Configuration.init(name, file.optional, objectID.optional)

private fun ProjectLocalizationInfo(
    development: XCSchema.Language,
    supported: Set<XCSchema.Language>
) : XCSchema.ProjectLocalizationInfo =
    XCSchema.ProjectLocalizationInfo.init(
        development,
        supported.asSwiftSet(::emptyLanguageSet, ::insertIntoLanguageSet)
    )

context(buildModel: IosApplicationBuildModel)
private val emptyXcodeProject: XCSchema.Project get() =
    Project(
        topLevelReferences = emptyList(),
        defaultConfigurationName = "Release".asConfigurationName,
        configurations = listOf(
            Configuration("Release".asConfigurationName),
            Configuration("Debug".asConfigurationName)
        ),
        targets = listOf(),
        localizationInfo = ProjectLocalizationInfo(
            development = "en".asLanguage,
            supported = setOf("Base".asLanguage)
        ),
        requiredCapabilities = emptySet(),
    )

context(buildModel: IosApplicationBuildModel)
internal fun XcodeProjectGenerationExtension.registerXcodeProjectGenerationTasks() {
    val project = emptyXcodeProject
    val json = project.jsonRepresentation().toByteArray()
    println(json.toString(Charsets.UTF_8))
}
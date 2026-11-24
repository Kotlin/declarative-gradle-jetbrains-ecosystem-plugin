package org.jetbrains.kotlin.gradle.declarative.xcode

import com.apple.xcode.project.format.XCSchema
import com.apple.xcode.project.format.XcodeProjectFormat.*
import org.swift.swiftkit.core.collections.SwiftDictionaryMap
import org.swift.swiftkit.core.collections.SwiftSet
import java.util.Optional
import kotlin.collections.MutableMap
import kotlin.collections.forEach

internal inline val <T : Any> T?.optional: Optional<T> get() = Optional.ofNullable(this)

// Sets

private abstract class MutableSwiftSet<T>(var delegate: SwiftSet<T>) : MutableSet<T>

private inline fun <T> Set<T>.toSwiftSet(emptySwiftSet: () -> SwiftSet<T>, insertCallback: (SwiftSet<T>, T) -> SwiftSet<T>): SwiftSet<T> =
    when(this) {
        is SwiftSet<T> -> this
        is MutableSwiftSet<T> -> delegate
        else -> fold(emptySwiftSet()) { acc, element -> insertCallback(acc, element) }
    }

private inline fun <T> buildSet(
    from: SwiftSet<T>,
    crossinline insertCallback: (SwiftSet<T>, T) -> SwiftSet<T>,
    crossinline removeCallback: (SwiftSet<T>, T) -> SwiftSet<T>,
    action: MutableSet<T>.() -> Unit
): Set<T> = object : MutableSwiftSet<T>(from) {
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

    override fun addAll(elements: Collection<T>): Boolean =
        elements.fold(false) { modified, element ->
            val added = add(element)
            modified || added
        }

    override fun removeAll(elements: Collection<T>): Boolean =
        elements.fold(false) { modified, element ->
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

}.apply(action)

@JvmName("toCapabilitySwiftSet")
internal fun Set<XCSchema.Capability>.toSwiftSet() = toSwiftSet(::emptyCapabilitySet, ::insertIntoCapabilitySet)

@JvmName("buildCapabilitySet")
internal inline fun buildSet(from: SwiftSet<XCSchema.Capability>? = null, action: MutableSet<XCSchema.Capability>.() -> Unit): Set<XCSchema.Capability> =
    buildSet(from ?: emptyCapabilitySet(), ::insertIntoCapabilitySet, ::removeFromCapabilitySet, action)

@JvmName("toLanguageSwiftSet")
internal fun Set<XCSchema.Language>.toSwiftSet() = toSwiftSet(::emptyLanguageSet, ::insertIntoLanguageSet)

@JvmName("buildLanguageSet")
internal inline fun buildSet(from: SwiftSet<XCSchema.Language>? = null, action: MutableSet<XCSchema.Language>.() -> Unit): Set<XCSchema.Language> =
    buildSet(from ?: emptyLanguageSet(), ::insertIntoLanguageSet, ::removeFromLanguageSet, action)

@JvmName("toPlatformFilterSwiftSet")
internal fun Set<XCSchema.PlatformFilter>.toSwiftSet() = toSwiftSet(::emptyPlatformFilterSet, ::insertIntoPlatformFilterSet)

@JvmName("buildPlatformFilterSet")
internal inline fun buildSet(from: SwiftSet<XCSchema.PlatformFilter>? = null, action: MutableSet<XCSchema.PlatformFilter>.() -> Unit): Set<XCSchema.PlatformFilter> =
    buildSet(from ?: emptyPlatformFilterSet(), ::insertIntoPlatformFilterSet, ::removeFromPlatformFilterSet, action)

@JvmName("toFolderMemberIDSwiftSet")
internal fun Set<XCSchema.FolderMemberID>.toSwiftSet() = toSwiftSet(::emptyFolderMemberIDSet, ::insertIntoFolderMemberIDSet)

@JvmName("buildFolderMemberIDSet")
internal inline fun buildSet(from: SwiftSet<XCSchema.FolderMemberID>? = null, action: MutableSet<XCSchema.FolderMemberID>.() -> Unit): Set<XCSchema.FolderMemberID> =
    buildSet(from ?: emptyFolderMemberIDSet(), ::insertIntoFolderMemberIDSet, ::removeFromFolderMemberIDSet, action)

@JvmName("toAssetTagSwiftSet")
internal fun Set<XCSchema.AssetTag>.toSwiftSet() = toSwiftSet(::emptyAssetTagSet, ::insertIntoAssetTagSet)

@JvmName("buildAssetTagSet")
internal inline fun buildSet(from: SwiftSet<XCSchema.AssetTag>? = null, action: MutableSet<XCSchema.AssetTag>.() -> Unit): Set<XCSchema.AssetTag> =
    buildSet(from ?: emptyAssetTagSet(), ::insertIntoAssetTagSet, ::removeFromAssetTagSet, action)

@JvmName("toLocalTargetReferenceSwiftSet")
internal fun Set<XCSchema.LocalTargetReference>.toSwiftSet() = toSwiftSet(::emptyLocalTargetReferenceSet, ::insertIntoLocalTargetReferenceSet)

@JvmName("buildLocalTargetReferenceSet")
internal inline fun buildSet(from: SwiftSet<XCSchema.LocalTargetReference>? = null, action: MutableSet<XCSchema.LocalTargetReference>.() -> Unit): Set<XCSchema.LocalTargetReference> =
    buildSet(from ?: emptyLocalTargetReferenceSet(), ::insertIntoLocalTargetReferenceSet, ::removeFromLocalTargetReferenceSet, action)

// Maps

private abstract class MutableSwiftDictionaryMap<K, V>(var delegate: SwiftDictionaryMap<K, V>) : MutableMap<K, V>

private inline fun <K, V> Map<K, V>.toSwiftDictionary(
    emptySwiftMap: () -> SwiftDictionaryMap<K, V>,
    putCallback: (SwiftDictionaryMap<K, V>, K, V) -> SwiftDictionaryMap<K, V>
): SwiftDictionaryMap<K, V> =
    when (this) {
        is SwiftDictionaryMap<K, V> -> this
        is MutableSwiftDictionaryMap<K, V> -> delegate
        else -> entries.fold(emptySwiftMap()) { acc, [key, value] -> putCallback(acc, key, value) }
    }

private inline fun <K, V> buildMap(
    from: SwiftDictionaryMap<K, V>,
    crossinline putCallback: (SwiftDictionaryMap<K, V>, K, V) -> SwiftDictionaryMap<K, V>,
    crossinline removeCallback: (SwiftDictionaryMap<K, V>, K) -> SwiftDictionaryMap<K, V>,
    action: MutableMap<K, V>.() -> Unit,
): Map<K, V> = object : MutableSwiftDictionaryMap<K, V>(from) {
    // TODO: ensure mutability
    override val keys: MutableSet<K> get() = delegate.keys
    override val values: MutableCollection<V> get() = delegate.values
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = delegate.entries

    override fun put(key: K, value: V): V? {
        val result = delegate[key]
        delegate = putCallback(delegate, key, value)
        return result
    }

    override fun remove(key: K): V? {
        val result = delegate[key]
        delegate = removeCallback(delegate, key)
        return result
    }

    override fun putAll(from: Map<out K, V>) = from.forEach { put(it.key, it.value) }

    override fun clear() = delegate.clear()

    override val size: Int get() = delegate.size

    override fun isEmpty(): Boolean = delegate.isEmpty()

    override fun containsKey(key: K): Boolean = delegate.containsKey(key)

    override fun containsValue(value: V): Boolean = delegate.containsValue(value)

    override fun get(key: K): V? = delegate[key]

}.apply(action)

private abstract class MutableSwiftSetDictionaryMap<K, V>(var delegate: SwiftDictionaryMap<K, SwiftSet<V>>) : MutableMap<K, Set<V>>

private inline fun <K, V> Map<K, Set<V>>.toSwiftDictionary(
    emptySwiftMap: () -> SwiftDictionaryMap<K, SwiftSet<V>>,
    emptySwiftSet: () -> SwiftSet<V>,
    insertCallback: (SwiftSet<V>, V) -> SwiftSet<V>,
    putCallback: (SwiftDictionaryMap<K, SwiftSet<V>>, K, SwiftSet<V>) -> SwiftDictionaryMap<K, SwiftSet<V>>
): SwiftDictionaryMap<K, SwiftSet<V>> =
    when (this) {
        is SwiftDictionaryMap<K, out Set<V>> -> @Suppress("UNCHECKED_CAST") (this as SwiftDictionaryMap<K, SwiftSet<V>>)
        is MutableSwiftSetDictionaryMap<K, V> -> delegate
        else -> entries.fold(emptySwiftMap()) { acc, [key, value] -> putCallback(acc, key, value.toSwiftSet(emptySwiftSet, insertCallback)) }
    }

private inline fun <K, V> buildMap(
    from: SwiftDictionaryMap<K, SwiftSet<V>>,
    crossinline emptySwiftSet: () -> SwiftSet<V>,
    crossinline insertCallback: (SwiftSet<V>, V) -> SwiftSet<V>,
    crossinline putCallback: (SwiftDictionaryMap<K, SwiftSet<V>>, K, SwiftSet<V>) -> SwiftDictionaryMap<K, SwiftSet<V>>,
    crossinline removeCallback: (SwiftDictionaryMap<K, SwiftSet<V>>, K) -> SwiftDictionaryMap<K, SwiftSet<V>>,
    action: MutableMap<K, Set<V>>.() -> Unit,
): Map<K, Set<V>> = object : MutableSwiftSetDictionaryMap<K, V>(from) {
    // TODO: ensure mutability
    override val keys: MutableSet<K> get() = delegate.keys
    override val values: MutableCollection<Set<V>> get() = delegate.values.mapTo(mutableSetOf()) { it }
    override val entries: MutableSet<MutableMap.MutableEntry<K, Set<V>>> get() = delegate.entries.mapTo(mutableSetOf()) { [key, value] ->
        object : MutableMap.MutableEntry<K, Set<V>> {
            override fun setValue(newValue: Set<V>): Set<V> = put(key, newValue) ?: newValue
            override val key: K get() = key
            override val value: Set<V> get() = value
        }
    }

    override fun put(key: K, value: Set<V>): Set<V>? {
        val result = delegate[key]
        delegate = putCallback(delegate, key, value.toSwiftSet(emptySwiftSet, insertCallback))
        return result
    }

    override fun remove(key: K): Set<V>? {
        val result = delegate[key]
        delegate = removeCallback(delegate, key)
        return result
    }

    override fun putAll(from: Map<out K, Set<V>>) = from.forEach { put(it.key, it.value) }

    override fun clear() = delegate.clear()

    override val size: Int get() = delegate.size

    override fun isEmpty(): Boolean = delegate.isEmpty()

    override fun containsKey(key: K): Boolean = delegate.containsKey(key)

    override fun containsValue(value: Set<V>): Boolean = delegate.containsValue(value)

    override fun get(key: K): Set<V>? = delegate[key]

}.apply(action)

@JvmName("toStringToBuildSettingSwiftDictionary")
internal fun Map<String, XCSchema.BuildSetting>.toSwiftDictionary(): SwiftDictionaryMap<String, XCSchema.BuildSetting> =
    toSwiftDictionary(::emptyStringToBuildSettingDictionary, ::putIntoStringToBuildSettingDictionary)

@JvmName("buildStringToBuildSettingMap")
internal inline fun buildMap(from: SwiftDictionaryMap<String, XCSchema.BuildSetting>? = null, action: MutableMap<String, XCSchema.BuildSetting>.() -> Unit): Map<String, XCSchema.BuildSetting> =
    buildMap(from ?: emptyStringToBuildSettingDictionary(), ::putIntoStringToBuildSettingDictionary, ::removeKeyFromStringToBuildSettingDictionary, action)

@JvmName("toFolderMemberIDToStringSwiftDictionary")
internal fun Map<XCSchema.FolderMemberID, String>.toSwiftDictionary(): SwiftDictionaryMap<XCSchema.FolderMemberID, String> =
    toSwiftDictionary(::emptyFolderMemberIDToStringDictionary, ::putIntoFolderMemberIDToStringDictionary)

@JvmName("buildFolderMemberIDToStringMap")
internal inline fun buildMap(from: SwiftDictionaryMap<XCSchema.FolderMemberID, String>? = null, action: MutableMap<XCSchema.FolderMemberID, String>.() -> Unit): Map<XCSchema.FolderMemberID, String> =
    buildMap(from ?: emptyFolderMemberIDToStringDictionary(), ::putIntoFolderMemberIDToStringDictionary, ::removeKeyFromFolderMemberIDToStringDictionary, action)

@JvmName("toFolderMemberIDToPlatformFiltersSwiftDictionary")
internal fun Map<XCSchema.FolderMemberID, Set<XCSchema.PlatformFilter>>.toSwiftDictionary(): SwiftDictionaryMap<XCSchema.FolderMemberID, SwiftSet<XCSchema.PlatformFilter>> =
    toSwiftDictionary(::emptyFolderMemberIDToPlatformFiltersDictionary, ::emptyPlatformFilterSet, ::insertIntoPlatformFilterSet, ::putIntoFolderMemberIDToPlatformFiltersDictionary)

@JvmName("buildFolderMemberIDToPlatformFiltersMap")
internal inline fun buildMap(from: SwiftDictionaryMap<XCSchema.FolderMemberID, SwiftSet<XCSchema.PlatformFilter>>? = null, action: MutableMap<XCSchema.FolderMemberID, Set<XCSchema.PlatformFilter>>.() -> Unit): Map<XCSchema.FolderMemberID, Set<XCSchema.PlatformFilter>> =
    buildMap(from ?: emptyFolderMemberIDToPlatformFiltersDictionary(), ::emptyPlatformFilterSet, ::insertIntoPlatformFilterSet, ::putIntoFolderMemberIDToPlatformFiltersDictionary, ::removeKeyFromFolderMemberIDToPlatformFiltersDictionary, action)

@JvmName("toFolderMemberIDToBuildFileAttributesSwiftDictionary")
internal fun Map<XCSchema.FolderMemberID, XCSchema.BuildFileAttributes>.toSwiftDictionary(): SwiftDictionaryMap<XCSchema.FolderMemberID, XCSchema.BuildFileAttributes> =
    toSwiftDictionary(::emptyFolderMemberIDToBuildFileAttributesDictionary, ::putIntoFolderMemberIDToBuildFileAttributesDictionary)

@JvmName("buildFolderMemberIDToBuildFileAttributesMap")
internal inline fun buildMap(from: SwiftDictionaryMap<XCSchema.FolderMemberID, XCSchema.BuildFileAttributes>? = null, action: MutableMap<XCSchema.FolderMemberID, XCSchema.BuildFileAttributes>.() -> Unit): Map<XCSchema.FolderMemberID, XCSchema.BuildFileAttributes> =
    buildMap(from ?: emptyFolderMemberIDToBuildFileAttributesDictionary(), ::putIntoFolderMemberIDToBuildFileAttributesDictionary, ::removeKeyFromFolderMemberIDToBuildFileAttributesDictionary, action)

@JvmName("toFolderMemberIDToAssetTagsSwiftDictionary")
internal fun Map<XCSchema.FolderMemberID, Set<XCSchema.AssetTag>>.toSwiftDictionary(): SwiftDictionaryMap<XCSchema.FolderMemberID, SwiftSet<XCSchema.AssetTag>> =
    toSwiftDictionary(::emptyFolderMemberIDToAssetTagsDictionary, ::emptyAssetTagSet, ::insertIntoAssetTagSet, ::putIntoFolderMemberIDToAssetTagsDictionary)

@JvmName("buildFolderMemberIDToAssetTagsMap")
internal inline fun buildMap(from: SwiftDictionaryMap<XCSchema.FolderMemberID, SwiftSet<XCSchema.AssetTag>>? = null, action: MutableMap<XCSchema.FolderMemberID, Set<XCSchema.AssetTag>>.() -> Unit): Map<XCSchema.FolderMemberID, Set<XCSchema.AssetTag>> =
    buildMap(from ?: emptyFolderMemberIDToAssetTagsDictionary(), ::emptyAssetTagSet, ::insertIntoAssetTagSet, ::putIntoFolderMemberIDToAssetTagsDictionary, ::removeKeyFromFolderMemberIDToAssetTagsDictionary, action)

@JvmName("toFolderMemberIDToFileTypeIDSwiftDictionary")
internal fun Map<XCSchema.FolderMemberID, XCSchema.FileTypeID>.toSwiftDictionary(): SwiftDictionaryMap<XCSchema.FolderMemberID, XCSchema.FileTypeID> =
    toSwiftDictionary(::emptyFolderMemberIDToFileTypeIDDictionary, ::putIntoFolderMemberIDToFileTypeIDDictionary)

@JvmName("buildFolderMemberIDToFileTypeIDMap")
internal inline fun buildMap(from: SwiftDictionaryMap<XCSchema.FolderMemberID, XCSchema.FileTypeID>? = null, action: MutableMap<XCSchema.FolderMemberID, XCSchema.FileTypeID>.() -> Unit): Map<XCSchema.FolderMemberID, XCSchema.FileTypeID> =
    buildMap(from ?: emptyFolderMemberIDToFileTypeIDDictionary(), ::putIntoFolderMemberIDToFileTypeIDDictionary, ::removeKeyFromFolderMemberIDToFileTypeIDDictionary, action)

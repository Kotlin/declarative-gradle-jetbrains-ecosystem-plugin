package org.jetbrains.kotlin.gradle.declarative.common.definitions

import org.gradle.jvm.toolchain.JvmVendorSpec

// JvmVendorSpec is an abstract class that is not possible to use in definitions
// This class is enum wrapper around it
@Suppress("UnstableApiUsage")
public enum class JvmVendor {
    ADOPTIUM,
    ADOPTOPENJDK,
    AMAZON,
    APPLE,
    AZUL,
    BELLSOFT,
    GRAAL_VM,
    HEWLETT_PACKARD,
    IBM,
    JETBRAINS,
    MICROSOFT,
    ORACLE,
    SAP,
    TENCENT;

    public fun toVendorSpec(): JvmVendorSpec = when (this) {
        ADOPTIUM -> JvmVendorSpec.ADOPTIUM
        ADOPTOPENJDK -> JvmVendorSpec.ADOPTOPENJDK
        AMAZON -> JvmVendorSpec.AMAZON
        APPLE -> JvmVendorSpec.APPLE
        AZUL -> JvmVendorSpec.AZUL
        BELLSOFT -> JvmVendorSpec.BELLSOFT
        GRAAL_VM -> JvmVendorSpec.GRAAL_VM
        HEWLETT_PACKARD -> JvmVendorSpec.HEWLETT_PACKARD
        IBM -> JvmVendorSpec.IBM
        JETBRAINS -> JvmVendorSpec.JETBRAINS
        MICROSOFT -> JvmVendorSpec.MICROSOFT
        ORACLE -> JvmVendorSpec.ORACLE
        SAP -> JvmVendorSpec.SAP
        TENCENT -> JvmVendorSpec.TENCENT
    }
}
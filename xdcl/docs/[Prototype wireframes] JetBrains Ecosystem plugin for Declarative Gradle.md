*⚠️⚠️⚠️ **This is a tabbed document** ⚠️⚠️⚠️*  
*Start with the Intro part especially if you are not familiar with Declarative Gradle concepts.*

*⚠️⚠️⚠️ **Platform vs Target** ⚠️⚠️⚠️*  
*In Kotlin documentation we use almost interchangeably platform and target especially in the KMP context. In this design we consistently use only one term \- platform, using compound nouns if disambiguation is needed.*

## What’s this about and scope

Declarative Gradle is a new (still in active development) way of writing build scripts next to existing Groovy and KTS build scripts. It will be a declarative subset of Kotlin language and it significantly changes how you read, write, and think about the build script. The hope is that our solution based on top of this technology will provide a 10x range improvement to developer experience especially on the onboarding part.

This is wireframe-like design for a prototype. We want to understand the technology, what future scope would look like, and validate key hypotheses ([LINK TO TAB]()) about DX improvements without investing too much development effort.

## Declarative Gradle primer

[Gradle provided Declarative Gradle overview](https://declarative.gradle.org/)

### From composition approach to declarative 

Right now in order to understand what exactly a module is you need to read the whole build script a few times and mentally compile the content. Let’s take as an example the `shared` subproject from KotlinConf app \- [LINK](https://github.com/JetBrains/kotlinconf-app/blob/main/shared/build.gradle.kts). Based on the plugins section it is most probably a KMP library that uses Compose Multiplaform. Based on the rest of the build script we can deduct which platforms it targets (Android, iOS, Web, Desktop).  
As a developer if I want to write such a subproject the first step is to decompose what **could** make such a thing when combined together. There is no easy way to say “give me all the basics I need to do X”. Each and every component you have to look for separately if you are not using the help of some template or a generator.  
Declarative Gradle finally flips this logic. A developer says what they want to accomplish for example `webApplication` and they can get a package of all the required plugins, oriented toward that goal configuration, and extension points for other frequently used optional plugins. If they want to they can further expand this by manually applying additional plugins. This top level entity is called **software type**.

### Explanation through a mock code

```kotlin
// build.gradle.dcl

webApplication { // Chosen project type - what we want to achieve. Each subproject has to use one (and only one) 

    compose {} // Enabling an optional functionality that comes prepackaged in the project type

    kotlin { // Enabling Kotlin support and Kotlin specific configuration
        serialization {} // Enabling serialization for Kotlin
    }

    packaging { // How the application will be packaged and distributed
        webpack {
            outputFileName = "kotlinconf-app.js"
        }
    }

    dependencies {
        implementation(projects.shared)
        implementation(libs.settings)
        implementation(libs.settings.serialization)
    }
    
    testing { // Testing-specific configuration is separate
        useJUnitPlatform()
        dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.settings.test)
        }
    }
}
```

As you can see there are some pretty significant changes:

* No plugins are imported or configured. They are packaged as `webApplication`, and some of them are optionally imported/applied like Compose Multiplatform.   
* There is a clear structure of the build file with use case oriented sections like `testing`, `packaging`. Some parts of the structure will look similar for different project types, but ultimately the scaffolding should streamline experience around that particular project type.  
* There is no arbitrary Kotlin code, the build script is declarative and can be interpreted without compilation.

Also \- **where does the `webApplication` come from?**

```kotlin
// settings.gradle.dcl - top level settings for the project 

rootProject.name = "KotlinConfApp"

include(":androidApp")
include(":ui-components")
include(":shared")
include(":backend")
include(":core")

pluginManagement {
    repositories {
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

plugins { // These plugins can provide software types, provide additional functionalites, or both
    id("org.jetbrains.ecosystem") version("1.0.0") // `webApplication` and others come from here
    id("com.android.ecosystem") version("9.4.0")
    id("org.springframework.ecosystem") version("4.1.0") // Provides only Spring software features
    id("org.mockito") version("3.0.0") // Provides only Mockito software features 
}

defaults { // Defaults for software types that will be applied to each subproject 

    library {
        jvmPlatform {
            toolchain {
                jvmLanguageVersion = 21
            }
        }
    }
}
```

Notice a few things:

1. Ecosystem plugins are imported here. They are the source of software types, but also/or provide software features that only expand capabilities  
2. `defaults` section for project-wide defaults. Right now to achieve a similar effect you have to use convention plugins. For just enabling features or setting values this is the new solution that should be significantly more lightweight. 

Also \- **what are those project features**?

* In short \- all blocks i.e. `block_name{}` are project features. They may be required or optional, and they have rules for binding i.e. where they can appear. For example `packaging{}` will bind to top level project types like `jvmApplication`, `dependencies{}` will bind to the top level, but also to `testing{}`, etc.   
* A plugin can add project features without introducing a project type and this will be the case for most of the plugins i.e. they will be enhancing project types with some additional features:

```kotlin
// build.gradle.dcl
jvmApplication {
    spring {} // Software feature enabling Spring, added by Spring ecosystem plugin

    kotlin {
        serialization {}
    }

    toolchain {
        jvmLanguageVersion = 24
    }

    packaging {
        spring { // Software feature from Spring for Spring-specific configuration
            bootBuildImage {
                environment.put("BP_JVM_CDS_ENABLED", "true")
            }
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
    }

    testing {
        mockito {} // Software feature enabling Mockito
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-test")
            implementation(kotlin("test-junit5"))
            runtimeOnly("org.junit.platform:junit-platform-launcher")
        }	
    }
}
```

*⚠️⚠️⚠️ **This is a tabbed document** ⚠️⚠️⚠️*  
*Start with the Intro part especially if you are not familiar with Declarative Gradle concepts.*

*⚠️⚠️⚠️ **Platform vs Target** ⚠️⚠️⚠️*  
*In Kotlin documentation we use almost interchangeably platform and target especially in the KMP context. In this design we consistently use only one term \- platform, using compound nouns if disambiguation is needed.*

# Key hypothesis and decisions

#### Simple should be easy, complex should be possible

We want to make core use cases as simple as possible even if it makes complex use cases more complicated.   
**Why?** The base level of complexity of the current DSL is too high.

#### Fully embrace modularization

Modules should represent a single product like iOS application or Android application or a library and never combine multiple products under one umbrella.  
**Why?** This should improve the readability of the build scripts and aligns with the suggested approach by Declarative Gradle itself.

#### Product platforms are default level of abstractions

The current version of DSL operates primarily on the level of “atomic” platforms like `iosArm64`, `wasmJs`, etc. In the new design we operate primarily on product level platforms like `ios`, `web`, etc. For product platforms that don’t have multiple subplatforms like `jvm` or `android` there is no change.  
**Why?** We found out that the majority of the configuration in existing setups is on product platform level and this should significantly shorten and simplify the build scripts.

#### Easy migration from single platform to multiplatform

Extending the application or library to target additional platforms should be as much as possible only additive work adding the new platform and should require as few changes to existing setup as possible.  
**Why?** A lot of multiplatform applications don’t start as such and removing the friction should help with the KMP adoption.

#### Move away from explicit KMP. There are Kotlin applications that target one or more platforms

The user should not need to concern themselves with what is happening under the hood and should focus on what they want to accomplish.  
**Why?** For example there is a difference in behaviour between a single-platform KMP application for JVM and a “regular” JVM application.

#### Full symmetry between Java and Kotlin

The best way to ensure a good adding-Kotlin-to-Java-application experience is to make sure that apart from Kotlin specific configuration everything else is the same. If Java applications use our software types, the barrier for trying out Kotlin will be extremely low and very low-commitment as you don’t have to migrate your build script. Because of this we also want to go with **JetBrains Ecosystem** **instead of Kotlin Ecosystem**.  
**Why?** Java experience within the current solution is effectively outside of Kotlin scope and vice versa. In Declarative Gradle the ecosystem plugin effectively owns the general experience of authoring the build script. This means it also defines the experience for “the other language”. If Java experience is significantly different from Kotlin experience it may create additional friction in trying out Kotlin and improve the split within the ecosystem.  
*⚠️⚠️⚠️ **This is a tabbed document** ⚠️⚠️⚠️*  
*Start with the Intro part especially if you are not familiar with Declarative Gradle concepts.*

*⚠️⚠️⚠️ **Platform vs Target** ⚠️⚠️⚠️*  
*In Kotlin documentation we use almost interchangeably platform and target especially in the KMP context. In this design we consistently use only one term \- platform, using compound nouns if disambiguation is needed.*

## Notes

1. The design was based on a version of [https\://github.com/JetBrains/kotlinconf-app](https://github.com/JetBrains/kotlinconf-app) from the end of Nov '25. At that time the app was not yet fully modularized, while we adopted full modularization in the following design.  
2. We mocked DSL for some plugins for now like `aboutLibraries`, final DSL would be provided by the plugin authors themselves.

## Kotlinconf App

#### settings.gradle.dcl

```kotlin
pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("android")
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.jetbrains.ecosystem") version("1.0.0")
    id("com.android.ecosystem") version("9.4.0")
    id("org.jetbrains.ktor.ecosystem") version("10.0.0")
}

rootProject.name = "KotlinConfApp"

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("android")
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

defaults {
    library {
        platforms = listOf(android, jvm, ios, web) // The platforms are enums

        jvmPlatform {
            toolchain {
                jvmLanguageVersion = 21
            }
        }

        androidPlatform {
            compileSdk = libs.versions.android.compileSdk
            defaultConfig {
                minSdk = libs.versions.android.minSdk
            }
            jvmTarget = JVM_11
            java {
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_11
                }
            }
        }

        defaults { // Defaults for optional block. Not yet implemented by Gradle
            compose {
                resources {
                    packageOfResClass = "org.jetbrains.kotlinconf.generated.resources"
                }
            }
        }
    }
}

include(":androidApp")
include(":ui-components")
include(":shared")
include(":backend")
include(":core")
```

#### androidApp

```kotlin
androidApplication {

    ... // DSL designed by Gradle/AGP team 

    compose {} // Explicitely applies Compose Multiplatform
}
```

#### desktopApp

```kotlin
jvmApplication {
    mainClass = "org.jetbrains.kotlinconf.MainKt"

    compose {}

    dependencies {
        implementation(projects.shared)
        implementation(compose.desktop.currentOs)
        implementation(libs.kotlinx.coroutines.swing)
        implementation(libs.slf4j.nop)
    }
}
```

#### backend

```kotlin
jvmApplication {
    mainClass = "io.ktor.server.netty.EngineMain"

    ktor {
        withDevelopmentBuild {} // applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
    }

    packaging {
        jib {
            container {
                mainClass = "io.ktor.server.netty.EngineMain"
            }
        }
    }

    dependencies {
        implementation(projects.core)
        implementation(libs.ktor.server.netty)
        implementation(libs.ktor.server.auth)
        implementation(libs.ktor.serialization.kotlinx.json)
        implementation(libs.ktor.client.cio)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.server.status.pages)
        implementation(libs.ktor.server.default.headers)
        implementation(libs.ktor.server.cors)
        implementation(libs.ktor.server.content.negotiation)
        implementation(libs.ktor.server.call.logging)
        implementation(libs.ktor.server.conditional.headers)
        implementation(libs.ktor.server.compression)
        implementation(libs.ktor.server.partial.content)
        implementation(libs.ktor.server.auto.head.response)
        implementation(libs.ktor.server.forwarded.header)
        implementation(libs.ktor.server.config.yaml)
        implementation(libs.ktor.server.swagger)

        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)

        implementation(libs.exposed.core)
        implementation(libs.exposed.dao)
        implementation(libs.exposed.jdbc)
        implementation(libs.h2)
        implementation(libs.postgresql)

        implementation(libs.koin.ktor)
        implementation(libs.koin.core)
        implementation(libs.koin.slf4j)

        implementation(libs.hikaricp)
        implementation(libs.logback.classic)
    }

    testing {
        useJUnitPlatform()
        dependencies {
            implementation(libs.ktor.server.test.host)
            implementation(libs.kotlin.test)
        }
    }
}
```

#### 

#### webApp

```kotlin
webApplication {
    packaging {
        webpack {
            outputFileName = "kotlinconf-app.js"
        }
    }

    dependencies {
        implementation(projects.shared)
    }
}
```

#### ui-components

```kotlin
library { // nonAndroidMain custom shared source set is not covered
    kotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    compose {
        resources {
            publicResClass = true
            nameOfResClass = "UiRes"
        }
    }

    androidPlatform { // Software feature provided by Android External Target plugin
        namespace = "com.jetbrains.kotlinconf.ui"
    }

    dependencies {
        api(libs.compose.runtime)
        api(libs.compose.foundation)
        api(libs.compose.animation)
        implementation(libs.compose.material.ripple)
        api(libs.compose.components.resources)
        implementation(libs.compose.ui.tooling.preview)

        implementation(libs.coil.compose)
        implementation(libs.coil.network.ktor3)
        implementation(libs.androidx.lifecycle.runtime.compose)
        implementation(libs.androidx.lifecycle.viewmodel.compose)

        implementation(libs.multiplatform.markdown.renderer)

        iosPlatform {
            implementation(libs.ktor.client.darwin)
        }

        androidPlatform {
            implementation(libs.ktor.client.okhttp)
            debugImplementation(libs.compose.ui.tooling)
        }

        jvmPlatform {
            implementation(libs.ktor.client.okhttp)
        }

        webPlatform {
            implementation(libs.ktor.client.js)
        }
    }
}
```

#### shared

```kotlin
library {  // nonAndroidMain custom shared source set is not covered
    kotlin {
        serialization { }
        parcelize { }
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    compose {}

    aboutLibraries {
        library {
            duplicationMode = DuplicateMode.MERGE
            duplicationRule = DuplicateRule.SIMPLE
        }
        export {
            outputFile = file("src/commonMain/composeResources/files")
        }
    }

    androidPlatform { // Software feature provided by Android External Target plugin
        namespace = "org.jetbrains.kotlinconf"
    		
        kotlin {
            compilerOptions {
                freeCompilerArgs.addAll(
                    "-P",
"plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=org.jetbrains.kotlinconf.zoomable.internal.AndroidParcelize"
                )
       	    }
        }
    }

    publishing {
        iosFramework {
            baseName = "shared"
            isStatic = true
        }
    }

    dependencies {
        api(projects.core)
        api(projects.uiComponents)

        api(libs.compose.runtime)
        api(libs.compose.foundation)
        api(libs.compose.animation)
        api(libs.compose.components.resources)

        api(libs.koin.compose.viewmodel.navigation)

        api(libs.ktor.client.logging)
        api(libs.ktor.serialization.kotlinx.json)
        api(libs.ktor.client.content.negotiation)
        api(libs.ktor.utils)

        implementation(libs.kotlinx.datetime)

        implementation(libs.androidx.lifecycle.runtime.compose)
        implementation(libs.androidx.navigation3.ui)
        implementation(libs.androidx.lifecycle.viewmodel.navigation3)
        implementation(libs.ktor.client.core)

        implementation(libs.aboutlibraries.core)

        // Multiplatform Settings
        implementation(libs.settings)
        implementation(libs.settings.serialization)
        implementation(libs.settings.observable)
        implementation(libs.settings.coroutines)

        api(libs.kmpnotifier)

        implementation(libs.doistx.normalize)

        iosPlatform {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.darwin)
        }

        androidPlatform {
            implementation(libs.android.svg)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.preference)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.ktor.client.okhttp)
            debugImplementation(libs.compose.ui.tooling)
        }

        jvmPlatform {
            implementation(libs.ktor.client.okhttp)
            implementation(compose.desktop.currentOs)
            implementation(libs.android.svg)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.slf4j.nop)
        }

        webPlatform {
            implementation(libs.ktor.client.js)
            implementation(npm("@js-joda/timezone", "2.3.0"))
        }
    }

    testing {
        dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.settings.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
```

#### core

```kotlin
library {
    kotlin {
        serialization { }
    }

    androidPlatform { // Software feature provided by Android External Target plugin
        namespace = "com.jetbrains.kotlinconf.model"
    }

    dependencies {
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.serialization.core)
        api(libs.kotlinx.serialization.json)
    }
}
```

*⚠️⚠️⚠️ **This is a tabbed document** ⚠️⚠️⚠️*  
*Start with the Intro part especially if you are not familiar with Declarative Gradle concepts.*

*⚠️⚠️⚠️ **Platform vs Target** ⚠️⚠️⚠️*  
*In Kotlin documentation we use almost interchangeably platform and target especially in the KMP context. In this design we consistently use only one term \- platform, using compound nouns if disambiguation is needed.*

## Notes

1. The design was based on a version of [https\://github.com/sdeleuze/spring-petklinik](https://github.com/sdeleuze/spring-petklinik) from the end of Nov '25.  
2. We mocked DSL for Spring blocks for the purpose of the prototype. DSL for it would be provided by Spring themselves.

## PetKlinik

#### `settings.gradle.dcl`

```kotlin
pluginManagement {
    repositories {
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        gradlePluginPortal()
    }
}

plugins {	
    id("org.jetbrains.ecosystem") version("1.0.0")
    id("org.springframework.ecosystem") version("4.0.0")
    id("org.jacoco") version("5.4.0")
}

rootProject.name = "PetKlinik"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
    }
}

defaults {
    jvmApplication {
        spring {}

        toolchain {
            jvmLanguageVersion = 24
        }

        packaging {
            spring {
                bootBuildImage { // tasks.named<BootBuildImage>("bootBuildImage") in .kts
                    environment.put("BP_JVM_CDS_ENABLED", "true")
                }
            }
        }

        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-web")
            implementation("org.jetbrains.kotlin:kotlin-reflect")
        }
        testing {
            useJUnitPlatform()
            dependencies {
                testImplementation("org.springframework.boot:spring-boot-starter-test")
                testImplementation(kotlin("test-junit5"))
                testRuntimeOnly("org.junit.platform:junit-platform-launcher")
            }	
        }
    }

    webApplication {
        kotlin {
            serialization {}
        }
    }

    library {
        platforms = listOf(jvm, web)

        kotlin {
            serialization {}
        }

        webPlatform {
            subplatforms = listOf(wasmJs) // Narrowing down web platform to only WasmJS
        }

    }
}

include("frontend", "backend", "shared", "ai")
```

#### `frontend`

```kotlin
webApplication {
    dependencies {
        implementation(project(":shared"))
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kotlinx.browser)
        implementation(libs.konform)
    }
}
```

#### `backend`

```kotlin
jvmApplication {

    spring {
        frameworkVersion = "7.0.0-SNAPSHOT"
    }

    kotlin {
        serialization {}
    }

    dependencies {
        implementation(project(":shared"))

        implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
        implementation("org.springframework.boot:spring-boot-starter-cache")
        implementation("org.springframework.boot:spring-boot-restclient")

        implementation(libs.kotlinx.html.jvm)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.konform)

        runtimeOnly("org.postgresql:postgresql")

        spring {
            developmentOnly("org.springframework.boot:spring-boot-docker-compose")
            resource(project(":frontend"), "static")
        }
    }
}
```

#### `shared`

```kotlin
library {
    spring {}

    dependencies { 
        implementation(kotlin("stdlib-common"))
        implementation(libs.kotlinx.html.common)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.konform)
        jvmPlatform {
            implementation(kotlin("stdlib"))
        }
    }

    testing {
        webPlatform {
            skip = true // wasmJs.browser.testTask.enabled = false in .kts
        }

        dependencies {
            implementation(kotlin("test"))
            jvmPlatform {
                implementation(kotlin("test-junit"))
            }
            webPlatform {
                implementation(libs.kotlinx.browser)
            }
        }
    }
}
```

#### `ai`

```kotlin
jvmApplication {

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    dependencies {
        implementation("org.springframework.ai:spring-ai-starter-model-openai")

        spring {
            imports {
                mavenBom("org.springframework.ai:spring-ai-bom:1.0.0")
            }
        }
    }
}
```

#### Full(er) `library` DSL

```kotlin
library {
    platforms = listOf(android, jvm, ios, watchOs, web)

    kotlin { // Settings for all Kotlin compilations
        serialization {}
        compilerOptions {
            apiVerison = 2.3
            ...
        }
    }

    java { // Settings for all Java compilations
        compilerOptions {
            compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
        }
    }
	
    jvmPlatform { // JVM platform specific configuration
        toolchain {
            jvmLanguageVersion = 21
        }
    }

    androidPlatform { // Project feature provided by Android External Target plugin
        namespace = "com.jetbrains.kotlinconf.model"
        compileSdk = 36 
        minSdk = 36
    }

    iosPlatform { 
        ... // iOS specific configuration
        iosArm64 { 
            ... // iosArm64 specific configuration
        }
    }

    watchosPlatform {
        // Product platform by default includes all target platforms
        // You can narrow it down explicitely
        subplatforms = listOf(watchosArm32, watchosArm64) 
    }

    publishing {
        iosFramework {
            baseName = "shared"
            isStatic = true
        }
        maven {
            ...
        }
    }

    dependencies {
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.serialization.core)
        api(libs.kotlinx.serialization.json)
        iosPlatform {
            api(libs.googlemaps.ios)
        }
    }

    testing {
        webPlatform {
            skip = true
        }

        dependencies {
            ...
            iosPlatform {
                ...
            }
        }
    }
}
```

#### Full(er) `jvmApplication` DSL

```kotlin
jvmApplication {

    mainClass = "org.jetbrains.kotlinconf.MainKt"

    compose {}

    kotlin {
        serialization {}
    }

    packaging {
        distribution {
            ...
        }
    }

    dependencies {
        implementation(project(":shared"))

        implementation(libs.kotlinx.html.jvm)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.konform)

        runtimeOnly("org.postgresql:postgresql")
    }

    testing {
        ...
        dependencies {
            ...
        }
    }
}
```

### Differences and transition between JVM-only library and JVM-KMP library

#### JVM-only `library`

If only the `jvm` platform is present the project is not a KMP project technology-wise e.g. there is no common compilation. In the current solution the analogous project would be a KGP JVM project.

```kotlin
library {
    platforms = listOf(jvm)

    kotlin { // Settings for all Kotlin compilations
        serialization {}
        compilerOptions {
            ...
        }
    }

    java { // Settings for all Java compilations
        compilerOptions {
            compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
        }
    }
	
    jvmPlatform { // Configuration that will be applied to both Java and Kotlin
        jvmTarget = JVM_11
        toolchain {
            jvmLanguageVersion = 21
        }
    }

    publishing {
        ...
    }

    dependencies {
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.serialization.core)
        api(libs.kotlinx.serialization.json)
    }

    testing {
        dependencies {
            ...
        }
    }
}
```

#### JVM-KMP `library`

By adding a `common` platform we explicitly state the intent for the library to be multiplatform at some point in time without adding a specific platform yet. This would be used either during the transition period or to be future-ready for multiplatform development. In the current solution the analogous project would be a KMP project with a single JVM platform enabled.

```kotlin
library {
    platforms = listOf(jvm, common)

    kotlin { // Settings for all Kotlin compilations
        serialization {}
        compilerOptions {
            ...
        }
    }

    java { // Settings for all Java compilations
        compilerOptions {
            compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
        }
    }
	
    jvmPlatform { // Configuration that will be applied to both Java and Kotlin
        jvmTarget = JVM_11
        toolchain {
            jvmLanguageVersion = 21
        }
    }

    publishing {
        ...
    }

    dependencies {
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.serialization.core)
        api(libs.kotlinx.serialization.json)
    }

    testing {
        dependencies {
            ...
        }
    }
```


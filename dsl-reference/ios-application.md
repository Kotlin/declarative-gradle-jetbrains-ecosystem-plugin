# iOS Application DSL Reference

```kotlin
iosApplication {
    subplatforms = listOf(iosArm64, iosSimulatorArm64, iosX64)
    
    kotlin { // Kotlin/Native compilation configuration
        compilerOptions { // Compiler arguments to apply for all Kotlin/Native compilations
            // KotlinNativeCompilerOptions members are available here, see more at:
            // https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.dsl/-kotlin-native-compiler-options/
        }
    }
     
    xcodeProject {
        // @IosApplicationEntryPoint
        entryPoint = auto | reference("...")
    }
    // signing & publishing
    
    // src/iosMain/kotlin/org/example/MainView.kt ->> UIViewController
    
    dependencies {
        api("...")
        // implementation("...")
        exportApi("...") // api + export to shared framework
        // spm dependencies only to kotlin code
    }
    
    // other apps use swift export
}
```
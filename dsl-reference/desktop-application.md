# Desktop Application DSL Reference

```declarative
desktopApplication {
    // Specifies the application distribution type, defaults to empty list
    platforms = listOf(jvm, native)
    
    jvmPlatform { // JVM application specific configurations
        modules += "java.desktop" // The JVM modules applied at runtime
        kotlin { // Kotlin JVM compilation configuration
            compilerOptions { // Compiler arguments to apply for all Kotlin JVM compilations
                // KotlinJvmCompilerOptions members are available here, see more at:
                // https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.dsl/-kotlin-jvm-compiler-options/
                jvmTarget = JVM_21 // Generated JVM bytecode target version
            }
        }
    }
    
    nativePlatform { // Native application specific configurations
        // Limits native macOS executable targets to the given subplatforms, defaults to all subplatforms (iosArm64, iosSimulatorArm64, and iosX64)
        // The property is used to instantiate the KMP target with an executable binary for each specified subplatform
        subplatforms = listOf(iosArm64, iosSimulatorArm64, iosX64)
        kotlin { // Kotlin Native compilation configuration, overrides common configuration
            compilerOptions { // Compiler arguments to apply for all Kotlin Native compilations
                // KotlinNativeCompilerOptions members are available here, see more at:
                // https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.dsl/-kotlin-native-compiler-options/
                moduleName = "desktop-app" // The ObjectiveC module name
            }
        }
    }
    
    packaging { // Application packaging configuration
        name = "..." // The application's (package) name
        version = "<any-format>" // The application's (package) version 
        copyright = "..." // The application's copyright information
        description = "..." // The description of the application
        vendor = "..." // The application's vendor
        license = layout.projectDirectory.file("...") // The application's license file
        // Specifies the OS platforms the application will be packaged/distributed for, defaults to empty list
        // `linux` and `windows` values are applicable iff the `jvm` platform is set, `macos` is applicable iff either `jvm` or `native` platform is set
        distributions = listOf(linux, macos, windows)

        linuxDistribution { // Linux platform options
            name = "..." // Optional property for the application's name on Linux platform, overrides the version value in `packaging { }`
            version = "<any-format>" // Optional property for the application's version on Linux platform, overrides the version value in `packaging { }`
            icon = layout.projectDirectory.file("...") // The application's icon on Linux plaftform (.png)
            formats = listOf(deb, rpm) // Limits the distribution formats for the application to packaged into on Linux platform, defaults to `deb` and `rpm`
            
            shortcut = true // Enables creating a Linux shortcut upon installation
            category = "..." // The `Group` value in RPM .spec or `Section` value of DEB control file
            menuGroup = "..." // The menu group the application is placed in

            distribution { // Distribution format configuration options
                deb { // .deb target format options
                    version = debVersion("<upstream-version>", <epoch-int> (optional), "<debianRevision>" (optional)) // Optional property for the application's version for .deb target, overrides the version value in `linuxPlatform { }`
                    maintainer = "..." // Optional property for the maintainer for the .deb distribution
                }

                rpm { // .rpm target format options
                    version = "<any-format>" // Optional property for the application's version for .rpm target, overrides the version value in `linuxPlatform { }`
                    licenseType = "..."// Optional property for the RPM license type
                }
            }
        }

        macosDistribution { // macOS platform options
            name = "..." // Optional property for the application's name for macOS platform, overrides the version value in `packaging { }`
            version = macosVersion(<major-int>, <minor-int> (optional), <patch-int> (optional)) // Optional property for the application's version for macOS platform, overrides the version value in `packaging { }`
            icon = layout.projectDirectory.file("...") // The application's icon on macOS plaftform (.icns)
            // Limits the distribution formats for the application to packaged into on macOS platform, defaults to `jvmDmg`, `jvmPkg`, `nativeExecutable`, and `nativeDmg`
            // The reason for distinguishing between `jvm` and `native` distributions is that we can distribute the desktop application ONLY as an JVM application (with jpackage).
            // However, on macOS it is also possible to package the application natively (provided the project is configured with native macOS targets).
            // It can be packaged as a native application (executable) or as both the executable AND a .dmg package
            formats = listOf(jvmDmg, jvmPkg, nativeExecutable, nativeDmg)
            
            buildVersion = iosVersion(<major-int>, <minor-int> (optional), <patch-int> (optional)) // The application's (build) version visible to the system, defaults to the application's version
            minimumSystemVersion = "..." // The minimum macOS version required to run the application
            bundleId = "..." // The unique identifier for the application, defaults to the reference of the main class
            // The category of the app in App Store (see more https://developer.apple.com/documentation/bundleresources/information-property-list/lsapplicationcategorytype), default is "public.app-category.utilities"
            // The property will be modelled as enum instead of raw strings in the format "public.app-category.<category>"
            category = utilities
            
            distribution { // Distribution configuration options
                jvm { // Configuration options for JVM-based distribution formats
                    dockName = "..." // The application's name displayed in the menu back, About menu, and the dock
                    appStoreDistribution = true // Enables the building and signing of the application for Apple App Store
                    
                    dmg { // .dmg target format options, specifies options for `jvmDmg` distribution
                        version = macosVersion(<major-int>, <minor-int> (optional), <patch-int> (optional)) // Optional property for th application's version for .dmg target, overrides the version value in `macosPlatform { }`
                        buildVersion = macosVersion(<major-int>, <minor-int> (optional), <patch-int> (optional)) // Optional property for the application's (build) version visible to the system for the .dmg target, overrides the build version value in `macosPlatform { }`
                    }

                    pkg { // .pkg target format options, specifies options for `jvmPkg` distribution
                        version = macosVersion(<major-int>, <minor-int> (optional), <patch-int> (optional)) // Optional application version for .pkg target, overrides the version value in `macosPlatform { }`
                        buildVersion = macosVersion(<major-int>, <minor-int> (optional), <patch-int> (optional)) // Optional property for the application's (build) version visible to the system for the .pkg target, overrides the build version value in `macosPlatform { }`
                    }

                    signing { // The signing options for macOS distribution
                        enable = true // Enables signing for declared macOS distribution
                        identity = "..." // The signing certificate's name
                        keychain = "..." // The path to the certificate's keychain
                        prefix = "..." //
                    }

                    notarization { // The notarization options for distribution outside Apple App Store
                        appleId = "..." // The owner's Apple ID
                        teamId = "..." // The owner's team ID
                        password = "..." // The password for this application
                    }
                }

                native { // Configuration options for Native-based distribution formats
                    dmg { // .dmg target format options, specifies options for `nativeDmg` distribution
                        version = macosVersion(<major-int>, <minor-int> (optional), <patch-int> (optional)) // Optional property for th application's version for .dmg target, overrides the version value in `macosPlatform { }`
                        buildVersion = macosVersion(<major-int>, <minor-int> (optional), <patch-int> (optional)) // Optional property for the application's (build) version visible to the system for the .dmg target, overrides the build version value in `macosPlatform { }`
                    }
                }
            }
        }

        windowsDistribution { // Windows platform options
            version = windowsVersion(<major-int>, <minor-int>, <build-int>) // Optional property for the application's version for Windows platform, overrides the version value in `packaging { }`
            icon = layout.projectDirectory.file("...") // The application's icon on Windows plaftform (.ico)
            // Limits the distribution formats for the application to packaged into on Windows platform, defaults to `msi` and `exe`
            formats = listOf(msi, exe)

            shortcut = true // Enables creating a Windows shortcut upon installation
            runWithConsole = true // Enables running the application with the console
            chooseInstallDirectory = true // Enables the option for users to choose where to install the application
            perUserInstallation = true // Enables local installation of the application on the current Windows profile
            upgradeUuid = "..." // The UUID associated with upgrades of this application; if a newer version of the package is installed, the old one is will be uninstalled
            
            menu { // Configuration options for the application access in Windows menu
                enable = true // Enables the users to access the application in Windows menu
                group = "..." // The group the application belongs to in Windows menu, defaults to "Unknown"
            }

            distribution { // Distribution configuration options
                msi { // .msi target format options
                    version = windowsVersion(<major-int>, <minor-int>, <build-int>) // Optional application version for .msi target, overrides the version value in `windowsPlatform { }`
                }

                exe { // .exe target format options
                    version = windowsVersion(<major-int>, <minor-int>, <build-int>) // Optional application version for .exe target, overrides the version value in `windowsPlatform { }`
                }
            }
        }
    }
}
```

### Notes and Questions on DSL:
1. The decision to include the distribution OS platforms in `packaging { ... }` is arbitrary, they could be declared in the top-level block. However, given the fact their configuration specifies the packaging form of each distribution, it made sense to declare them here.
2. `distribution { ... }` does not provide any functionality. It only serves to create the mental separation between the declaration of the enclosing OS distribution and specific distribution format options.
  - *Question:* If it makes sense to keep the block, what would be a better name for it?
3. A possible design for the `notarization { ... }` block could be a single property representing the path to the file with the notarization information/secrets for privacy, or only allow users to declare them in Gradle's `.properties` files
4. The main difference between Compose Desktop DSL and our `desktopApplication` is that, for the former, the packaging information is separate between _JVM application_ and _Native application_. But in the latter, it is split between platforms (which are subsumed by either the JVM application or native application) because the information for the same platforms (only macOS in this case) on different applications, usually, tends to be the same. Therefore, the configuration of the `packaging { ... }` properties `name`, `version`, `icon`, `copyright`, `description`, `vendor`, and `licence` are shared between JVM and Native application platforms.
  - *Question:* Should we allow users to provide separate specification of these properties between JVM and native?
5. The same principle is applied to `macosDistribution` as we want the user to focus on how the distribution of the application looks like for a specific OS platform, instead of focusing on which Kotlin platform is the application compiled on.
  - *Question:* Would it be useful to have the separation of `jvmMacosDistribution { ... }` and `nativeMacosDistribution { ... }` blocks instead of a single `macosDistribution { ... }` block?

## Notes on the BuildModel API for `desktopApplication`

### DesktopApplicationBuildModel (`desktopApplication { ... }`)
Contains the low-level configuration of JVM and native (macOS) applications profiles
- For JVM application:
  1. The main class
  2. A custom JAVA_HOME
  3. Custom JPackage arguments
  4. Custom JPackage JVM options
  5. Proguard settings of the RELEASE build type
  6. Specification of runtime files from either:
     - a source set
     - a specific (Kotlin) target
     - specific file collection + main jar + dependent tasks
- For Native application: currently Compose provide *no additional options* here

### MacosDistributionBuildModel (`macosDistribution { ... }`)
Contains the low-level configuration for additional content and signing (both on JVM application side)
- For additional content:
  1. `provisioningProfile` file property
  2. `runtimeProvisioningProfile` file property
- For signing:
  1. `entitlementsFile` file property
  2. `runtimeEntitlementsFile` file property
- Extra raw configuration for `Info.plist`
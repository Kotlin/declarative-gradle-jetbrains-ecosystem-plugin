jvmApplication {
    kotlin {
        serialization {
            enabledFormats = listOf("json")
        }
    }

    dependencies {
        implementation(project(":shared"))
        implementation("org.springframework.boot:spring-boot-starter-webmvc") {
            exclude(mapOf("group" to "org.springframework.boot", "module" to "spring-boot-starter-jackson"))
        }
        implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
        implementation("org.springframework.boot:spring-boot-starter-cache")
        implementation("org.springframework.boot:spring-boot-restclient")
        implementation("org.springframework.boot:spring-boot-kotlinx-serialization-json")

        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0")
        implementation("io.konform:konform:0.11.1")

        runtimeOnly("org.postgresql:postgresql")

        spring {
            developmentOnly("org.springframework.boot:spring-boot-docker-compose")
        }

        resources {
            resource(project(":frontend"))
        }
    }

    testing {
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-test")
            implementation("org.jetbrains.kotlin:kotlin-test-junit5")
            runtimeOnly("org.junit.platform:junit-platform-launcher")
        }
    }
}
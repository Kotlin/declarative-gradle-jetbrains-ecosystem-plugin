jvmApplication {
    kotlin {
        compilerOptions {
            freeCompilerArgs += listOf("-Xjsr305=strict")
        }
    }

    dependencies {
        implementation(platform("org.springframework.ai:spring-ai-bom:2.0.0-M1"))
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.ai:spring-ai-starter-model-openai")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
    }
}
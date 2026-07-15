import org.gradle.api.tasks.testing.Test

plugins {
    kotlin("jvm") version "1.9.24" apply false
    kotlin("plugin.spring") version "1.9.24" apply false
    kotlin("plugin.jpa") version "1.9.24" apply false
    id("org.springframework.boot") version "3.3.2" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
}

allprojects {
    group = "com.shortener"
    version = "1.0.0"
    repositories { mavenCentral() }
}

subprojects {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

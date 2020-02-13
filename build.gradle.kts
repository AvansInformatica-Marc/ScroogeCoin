import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.61"
}

group = "nl.avans"
version = "1.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(fileTree(mapOf(
        "include" to listOf("*.jar"),
        "dir" to "src/main/libs"
    )))
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(fileTree(mapOf(
        "include" to listOf("*.jar"),
        "dir" to "src/test/libs"
    )))
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.6.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "11"
    }
}

plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.allopen") version "2.4.10"
    kotlin("plugin.jpa") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
    id("com.google.devtools.ksp") version "2.3.13"
    id("eu.vendeli.telegram-bot") version "testing"
    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("http://192.168.1.142:8081")
        isAllowInsecureProtocol = true
        // Only KtGram artifacts live here; keeps an offline repo from breaking other resolution.
        content { includeGroupAndSubgroups("eu.vendeli") }
    }
}

val quarkusPlatformGroupId = project.property("quarkusPlatformGroupId") as String
val quarkusPlatformArtifactId = project.property("quarkusPlatformArtifactId") as String
val quarkusPlatformVersion = project.property("quarkusPlatformVersion") as String

ktGram {
    // Quarkus ships its own logging (JBoss LogManager + slf4j bridge). Letting KtGram add
    // logback-classic in afterEvaluate breaks configuration: Quarkus has already resolved
    // quarkusDevRuntimeClasspathConfiguration by then, so ':implementation' can't be mutated.
    handleLoggingProvider = false
}

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-config-yaml:3.40.1.2")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin:3.40.1.2")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-client-jackson:3.40.1.2")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-kotlin")
//    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-scheduler:3.40.1.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("io.quarkus:quarkus-junit")
    testImplementation("io.rest-assured:rest-assured")
    implementation("eu.vendeli:quarkus-ktgram:testing")
}

group = "org.qmained"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        javaParameters = true
    }
}

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.star_zero.gradle.githook.Githook
import com.star_zero.gradle.githook.GithookExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.3.21"
    id("org.jlleitschuh.gradle.ktlint") version "7.1.0"
    id("com.star-zero.gradle.githook") version "1.1.0"
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

fun version(): String {
    val buildNumber = System.getProperty("BUILD_NUM")
    val version = "0.1" + if (buildNumber.isNullOrEmpty()) "-SNAPSHOT" else ".$buildNumber"
    println("building version $version")
    return version
}

val projectVersion = version()

group = "io.hexlabs"
val artifactId = "propex-orders-api"
version = projectVersion

val http4kVersion = "3.115.1"

repositories {
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/hexlabsio/kloudformation")
}

sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin")
        }
    }
    test {
        java {
            srcDirs("src/test/kotlin", "stack")
        }
    }
}

val shadowJar by tasks.getting(ShadowJar::class) {
    archiveClassifier.set("uber")
    manifest {
        attributes(mapOf("Main-Class" to "io.hexlabs.kloudformation.runner.DeployKt"))
    }
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile("org.http4k:http4k-core:$http4kVersion")
    compile("org.http4k:http4k-format-jackson:$http4kVersion")
    compile("org.http4k:http4k-serverless-lambda:$http4kVersion")
    compile("org.jetbrains.exposed:exposed:0.13.5")
    runtime("org.postgresql:postgresql:42.2.5")
    compile("org.apache.logging.log4j:log4j-slf4j-impl:2.9.0")
    testImplementation("io.kloudformation:kloudformation:0.1.119")
    testImplementation("io.hexlabs:kloudformation-serverless-module:0.1.7")

    testImplementation("io.mockk:mockk:1.9.2.kotlin12")
    testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-test-junit5", version = "1.3.21")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "1.3.21")
    testRuntime(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.0.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

artifacts {
    add("archives", shadowJar)
}

configure<KtlintExtension> {
    outputToConsole.set(true)
    coloredOutput.set(true)
    reporters.set(setOf(ReporterType.CHECKSTYLE, ReporterType.JSON))
}

configure<GithookExtension> {
    githook {
        hooks {
            create("pre-push") {
                task = "build"
            }
        }
    }
}
plugins {
  idea
  java
  jacoco
  `maven-publish`

  id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
  id("io.gitlab.arturbosch.detekt") version "1.16.0-RC2"

  id("com.github.ben-manes.versions") version "0.38.0"
  kotlin("jvm") version "1.4.31"
  kotlin("kapt") version "1.4.31"

  id("org.springframework.boot") version "2.5.0-M2"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  kotlin("plugin.spring") version "1.5.0-M1"
}

object Versions {
  const val JAVA = "11"
}

fun isApplication(project: Project) = project.name.endsWith("authentication") || project.name.contains("endpoint")
fun isLibrary(project: Project) = project.name.endsWith("core") || project.name.endsWith("proto")

allprojects {
  apply(plugin = "idea")
  apply(plugin = "java")
  apply(plugin = "jacoco")

  apply(plugin = "org.jlleitschuh.gradle.ktlint")
  apply(plugin = "io.gitlab.arturbosch.detekt")

  apply(plugin = "com.github.ben-manes.versions")
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "org.jetbrains.kotlin.kapt")

  group = "com.ukonnra.wonderland.doorknob"
  version = "0.0.1"

  repositories {
    jcenter()
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
    maven { url = uri("https://jitpack.io") }
  }

  ktlint {
    version.set("0.40.0")
  }

  detekt {
    failFast = true
    config = files("$rootDir/detekt.yml")
    autoCorrect = true
    buildUponDefaultConfig = true
    reports {
      xml.enabled = true
      html.enabled = true
      txt.enabled = false
    }
  }

  tasks.detekt {
    jvmTarget = Versions.JAVA
  }
}

subprojects {
  apply(plugin = "org.springframework.boot")
  apply(plugin = "org.jetbrains.kotlin.plugin.spring")
  apply(plugin = "io.spring.dependency-management")

  kapt.includeCompileClasspath = false

  dependencies {
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
  }

  java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withSourcesJar()
    withJavadocJar()
  }

  tasks.compileKotlin {
    kotlinOptions {
      jvmTarget = Versions.JAVA
      freeCompilerArgs = listOf("-Xjsr305=strict")
    }
  }

  tasks.compileTestKotlin {
    kotlinOptions {
      jvmTarget = Versions.JAVA
      freeCompilerArgs = listOf("-Xjsr305=strict")
    }
  }

  tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    useJUnitPlatform()
  }

  configurations {
    developmentOnly
    runtimeClasspath {
      extendsFrom(configurations.developmentOnly.get())
    }
  }

  if (isApplication(project)) {
    dependencies {
      implementation("org.springframework.experimental:spring-graalvm-native:0.8.5")
      implementation("org.springframework:spring-context-indexer")
    }

    tasks.bootBuildImage {
      val imageTag = project.properties["imageTag"] ?: throw GradleException("imageTag should not be null")
      imageName = "ukonnra/${project.name}:$imageTag"
      builder = "paketobuildpacks/builder:tiny"
      environment = mapOf(
        "BP_BOOT_NATIVE_IMAGE" to "1",
        "BP_BOOT_NATIVE_IMAGE_BUILD_ARGUMENTS" to """
      -H:+AddAllCharsets
      -H:+ReportExceptionStackTraces
      -H:+PrintAnalysisCallTree
      --enable-all-security-services
      --enable-https
      --enable-http
      -Dspring.spel.ignore=false
      -Dspring.native.remove-yaml-support=true
        """.trimIndent()
      )

      if (System.getProperty("spring.profiles.active")?.equals("production") == true) {
        isPublish = true
        docker {
          publishRegistry {
            username = "ukonnra"
            email = "ukonnra@outlook.com"
            password = project.properties["dockerToken"]?.toString() ?: throw GradleException("imageTag should not be null")
          }
        }
      }
    }
  }

  if (isLibrary(project)) {
    apply(plugin = "maven-publish")

    publishing.publications.register("release", MavenPublication::class) {
      from(components["java"])
    }

    tasks.bootJar {
      enabled = false
    }

    tasks.jar {
      enabled = true
    }
  }
}

val codeCoverageReport = tasks.register<JacocoReport>("codeCoverageReport") {
  setDependsOn(subprojects.map { it.tasks.test })

  executionData(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))

  subprojects.forEach {
    sourceSets(it.sourceSets["main"])
  }

  reports {
    xml.isEnabled = true
    xml.destination = file("$buildDir/reports/jacoco/report.xml")
    html.isEnabled = true
    csv.isEnabled = false
  }
}

tasks.check {
  dependsOn(codeCoverageReport)
}

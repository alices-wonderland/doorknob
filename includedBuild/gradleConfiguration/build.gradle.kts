plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

repositories {
  gradlePluginPortal()
  maven { url = uri("https://repo.spring.io/release") }
}

private object Versions {
  const val KOTLIN = "1.4.32"
}

dependencies {
  implementation("org.springframework.boot:spring-boot-gradle-plugin:2.4.5")
  implementation("io.spring.gradle:dependency-management-plugin:1.0.11.RELEASE")
  implementation("org.springframework.experimental:spring-aot-gradle-plugin:0.9.2")
  implementation("org.jlleitschuh.gradle:ktlint-gradle:10.0.0")
  implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.17.0-RC2")
  implementation("com.github.ben-manes:gradle-versions-plugin:0.38.0")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}")
  implementation("org.jetbrains.kotlin:kotlin-allopen:${Versions.KOTLIN}")
}

gradlePlugin {
  plugins.register("project-configuration") {
    id = "project-configuration"
    implementationClass = "com.ukonnra.wonderland.doorknob.gradle.configure.ProjectConfigurationPlugin"
  }

  plugins.register("application-configuration") {
    id = "application-configuration"
    implementationClass = "com.ukonnra.wonderland.doorknob.gradle.configure.ApplicationConfigurationPlugin"
  }

  plugins.register("library-configuration") {
    id = "library-configuration"
    implementationClass = "com.ukonnra.wonderland.doorknob.gradle.configure.LibraryConfigurationPlugin"
  }
}

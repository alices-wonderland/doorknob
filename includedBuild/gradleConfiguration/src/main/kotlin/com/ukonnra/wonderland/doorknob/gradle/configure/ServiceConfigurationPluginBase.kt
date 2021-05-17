package com.ukonnra.wonderland.doorknob.gradle.configure

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import org.jetbrains.kotlin.allopen.gradle.SpringGradleSubplugin
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import java.net.URI

abstract class ServiceConfigurationPluginBase : ConfigurationPluginBase() {
  companion object {
    private const val JACKSON_VERSION = "2.12.3"
  }

  abstract fun doApply(target: Project)

  override fun apply(target: Project) {
    super.apply(target)

    target.apply<Kapt3GradleSubplugin>()
    target.apply<SpringGradleSubplugin>()
    target.apply<SpringBootPlugin>()
    target.apply<DependencyManagementPlugin>()

    target.repositories.apply {
      maven { url = URI.create("https://repo.spring.io/release") }
    }

    target.extensions.configure(KaptExtension::class.java) {
      includeCompileClasspath = false
    }

    target.configurations.all {
      // exclude(group = "org.springframework.boot", module = "spring-boot-starter-web")
      exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }

    target.dependencies {
      "implementation"("io.projectreactor.kotlin:reactor-kotlin-extensions")

      "implementation"("org.springframework:spring-context-indexer")

      "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin:${JACKSON_VERSION}")
      "implementation"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${JACKSON_VERSION}")
      "implementation"("org.jetbrains.kotlin:kotlin-reflect")
      "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
      "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:${COROUTINE_VERSION}")
      "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${COROUTINE_VERSION}")
      "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${COROUTINE_VERSION}")
      "implementation"("org.redisson:redisson:${Versions.REDISSON}")
      "testImplementation"("org.springframework.boot:spring-boot-starter-test")
      "testImplementation"("io.projectreactor:reactor-test")
    }

    doApply(target)
  }
}

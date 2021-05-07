package com.ukonnra.wonderland.doorknob.gradle.configure

import com.github.benmanes.gradle.versions.VersionsPlugin
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.KtlintPlugin

abstract class ConfigurationPluginBase : Plugin<Project> {
  companion object {
    internal val JAVA_VERSION = JavaVersion.VERSION_11
    internal const val COROUTINE_VERSION = "1.4.3"
  }

  override fun apply(target: Project) {
    target.apply<IdeaPlugin>()
    target.apply<JavaPlugin>()
    target.apply<JacocoPlugin>()
    target.apply<KtlintPlugin>()
    target.apply<DetektPlugin>()
    target.apply<VersionsPlugin>()
    target.apply<KotlinPluginWrapper>()

    target.group = "com.ukonnra.wonderland.doorknob"

    target.repositories.apply {
      mavenCentral()
      jcenter()
    }

    target.extensions.configure(JavaPluginExtension::class.java) {
      sourceCompatibility = JAVA_VERSION
      targetCompatibility = JAVA_VERSION
    }

    target.extensions.configure(KtlintExtension::class.java) {
      version.set("0.41.0")
    }

    target.extensions.configure(DetektExtension::class.java) {
      config = target.files("${target.rootDir}/detekt.yml")
      autoCorrect = true
      buildUponDefaultConfig = true
      reports {
        xml.enabled = true
        html.enabled = true
        txt.enabled = false
      }
    }

    target.extensions.configure(JacocoPluginExtension::class.java) {
      toolVersion = "0.8.7"
    }

    target.tasks.named(DetektPlugin.DETEKT_TASK_NAME, Detekt::class.java) {
      jvmTarget = JAVA_VERSION.majorVersion
    }

    target.tasks.named(JavaPlugin.TEST_TASK_NAME, Test::class.java) {
      finalizedBy(target.tasks.named("jacocoTestReport"))
      useJUnitPlatform()
    }

    target.tasks.named("clean", Delete::class.java) {
      delete("out", "logs")
    }

    listOf("compileKotlin", "compileTestKotlin").forEach {
      target.tasks.named(it, KotlinCompile::class.java) {
        kotlinOptions {
          jvmTarget = JAVA_VERSION.majorVersion
          freeCompilerArgs = listOf("-Xjsr305=strict")
        }
      }
    }
  }
}

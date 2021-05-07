package com.ukonnra.wonderland.doorknob.gradle.configure

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.kotlin.dsl.apply
import org.gradle.language.jvm.tasks.ProcessResources
import org.springframework.aot.gradle.SpringAotGradlePlugin
import org.springframework.aot.gradle.dsl.SpringAotExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

class ApplicationConfigurationPlugin : ServiceConfigurationPluginBase() {
  override fun doApply(target: Project) {
    target.apply<SpringAotGradlePlugin>()

    target.extensions.configure(SpringAotExtension::class.java) {
      removeSpelSupport.set(true)
      removeYamlSupport.set(true)
    }

    target.tasks.named("processAotTestResources", ProcessResources::class.java) {
      duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    target.tasks.named(SpringBootPlugin.BOOT_BUILD_IMAGE_TASK_NAME, BootBuildImage::class.java) {
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
        """.trimIndent()
      )

      if (System.getProperty("spring.profiles.active")?.equals("production") == true) {
        isPublish = true
        docker {
          publishRegistry {
            username = "ukonnra"
            email = "ukonnra@outlook.com"
            password =
              project.properties["dockerToken"]?.toString() ?: throw GradleException("dockerToken should not be null")
          }
        }
      }
    }
  }
}

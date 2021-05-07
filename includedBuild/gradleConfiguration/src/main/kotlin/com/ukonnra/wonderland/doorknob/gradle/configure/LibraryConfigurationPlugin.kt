package com.ukonnra.wonderland.doorknob.gradle.configure

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.bundling.BootJar

class LibraryConfigurationPlugin : ServiceConfigurationPluginBase() {
  override fun doApply(target: Project) {
    target.apply<MavenPublishPlugin>()
    target.extensions.configure(JavaPluginExtension::class.java) {
      withJavadocJar()
      withSourcesJar()
    }

    target.extensions.configure(PublishingExtension::class.java) {
      publications.register("release", MavenPublication::class) {
        from(target.components["java"])
        pom {
          licenses {
            license {
              name.set("The Apache License, Version 2.0")
              url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
          }
        }
      }
    }

    target.tasks.named(SpringBootPlugin.BOOT_JAR_TASK_NAME, BootJar::class.java) {
      enabled = false
    }

    target.tasks.named(JavaPlugin.JAR_TASK_NAME, Jar::class.java) {
      enabled = true
    }
  }
}

package com.ukonnra.wonderland.doorknob.gradle.configure

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.tasks.JacocoReport

class ProjectConfigurationPlugin : ConfigurationPluginBase() {
  override fun apply(target: Project) {
    super.apply(target)
    val codeCoverageReport = target.tasks.register<JacocoReport>("codeCoverageReport") {
      target.subprojects.map { it.tasks.named(JavaPlugin.TEST_TASK_NAME) }.forEach {
        dependsOn(it)
      }

      executionData(target.fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))

      target.subprojects.forEach {
        sourceSets((it.extensions.getByName("sourceSets") as SourceSetContainer)["main"])
      }

      reports {
        xml.isEnabled = true
        xml.destination = target.file("${target.buildDir}/reports/jacoco/report.xml")
        html.isEnabled = true
        csv.isEnabled = false
      }
    }

    target.tasks.named("check") {
      dependsOn(codeCoverageReport)
    }
  }
}

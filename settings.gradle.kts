pluginManagement {
  repositories {
    maven { url = uri("https://repo.spring.io/release") }
    gradlePluginPortal()
  }
}

rootProject.name = "doorknob"

include(
  ":doorknob-core",
  ":doorknob-authentication",
  ":doorknob-proto",
  ":doorknob-endpoint-restful"
)

includeBuild("includedBuild/gradleConfiguration")

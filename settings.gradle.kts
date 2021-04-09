pluginManagement {
  // resolutionStrategy {
  //   eachPlugin {
  //     if(requested.id.toString() == "com.ukonnra.wonderland.project")
  //       useModule("com.github.alices-wonderland.infrastructure:wonderland-project-gradle-plugin:feature~project-plugin-34b9f98a39-1")
  //   }
  // }

  repositories {
    maven { url = uri("https://repo.spring.io/release") }
    maven { url = uri("https://jitpack.io") }
    gradlePluginPortal()
    mavenLocal()
  }
}

rootProject.name = "doorknob"

include(
  ":doorknob-core",
  ":doorknob-authentication",
  ":doorknob-proto"
)

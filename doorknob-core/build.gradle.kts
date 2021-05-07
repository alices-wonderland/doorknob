import com.ukonnra.wonderland.doorknob.gradle.configure.Versions

plugins {
  id("library-configuration")
}

dependencies {
  implementation("com.github.alices-wonderland:annotations:develop-SNAPSHOT")
  kapt("com.github.alices-wonderland:annotations:develop-SNAPSHOT")

  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

  implementation("sh.ory.hydra:hydra-client:${Versions.HYDRA}")
}

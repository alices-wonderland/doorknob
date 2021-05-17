import com.ukonnra.wonderland.doorknob.gradle.configure.Versions

plugins {
  id("application-configuration")
}

dependencies {
  implementation(project(":infrastructure-core"))
  implementation(project(":doorknob-core"))

  implementation("org.springframework.experimental:spring-native-configuration:${Versions.SPRING_NATIVE}")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.security:spring-security-oauth2-resource-server")
  implementation("com.nimbusds:oauth2-oidc-sdk:${Versions.NIMBUS_OIDC}")

  implementation("org.springframework.session:spring-session-core")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  kapt("org.springframework.boot:spring-boot-configuration-processor")

  implementation("sh.ory.hydra:hydra-client:${Versions.HYDRA}")
}

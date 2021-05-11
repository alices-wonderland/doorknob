import com.ukonnra.wonderland.doorknob.gradle.configure.Versions

plugins {
  id("library-configuration")
}

version = "0.2.0"

dependencies {
  implementation(project(":infrastructure-core"))

  implementation("org.springframework.security:spring-security-oauth2-core")
  api("org.redisson:redisson-spring-boot-starter:${Versions.REDISSON}")

  implementation("sh.ory.hydra:hydra-client:${Versions.HYDRA}")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  testImplementation(project(":infrastructure-testsuite"))
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("org.springframework.boot:spring-boot-starter-security")
  testImplementation("com.nimbusds:oauth2-oidc-sdk:${Versions.NIMBUS_OIDC}")
}

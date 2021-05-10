plugins {
  id("application-configuration")
}

dependencies {
  implementation(project(":infrastructure-core"))
  implementation(project(":doorknob-core"))

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
}

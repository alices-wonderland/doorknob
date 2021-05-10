plugins {
  id("library-configuration")
}

dependencies {
  implementation("org.springframework.security:spring-security-oauth2-core")

  testImplementation(project(":infrastructure-testsuite"))
}

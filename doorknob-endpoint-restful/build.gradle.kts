dependencies {
  implementation(project(":doorknob-core"))

  implementation("javax.servlet:javax.servlet-api:4.0.1")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
}

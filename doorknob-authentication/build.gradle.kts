dependencies {
  implementation(project(":doorknob-core"))
  implementation(project(":doorknob-proto"))

  implementation("javax.servlet:javax.servlet-api:4.0.1")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.session:spring-session-core")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  kapt("org.springframework.boot:spring-boot-configuration-processor")

  implementation("sh.ory.hydra:hydra-client:${Versions.HYDRA}")

  implementation("com.github.joschi.jackson:jackson-datatype-threetenbp:2.12.2")
}

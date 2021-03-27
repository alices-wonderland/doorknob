dependencies {
  implementation(project(":doorknob-core"))
  implementation(project(":doorknob-proto"))

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.session:spring-session-core")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  kapt("org.springframework.boot:spring-boot-configuration-processor")

  implementation("com.github.joschi.jackson:jackson-datatype-threetenbp:2.12.2")
}

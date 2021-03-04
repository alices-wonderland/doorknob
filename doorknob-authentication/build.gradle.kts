
object Versions {
  const val HYDRA = "1.9.0"
  const val JACKSON = "2.12.1"
}

dependencies {
  implementation("com.github.alices-wonderland:infrastructure:develop-SNAPSHOT")
  implementation(project(":doorknob-proto"))

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.session:spring-session-core")
  developmentOnly("org.springframework.boot:spring-boot-devtools")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("sh.ory.hydra:hydra-client:${Versions.HYDRA}")
  implementation("com.github.joschi.jackson:jackson-datatype-threetenbp:${Versions.JACKSON}")
}

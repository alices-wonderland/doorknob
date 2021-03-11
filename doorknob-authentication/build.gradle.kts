
object Versions {
  const val HYDRA = "1.9.0"
}

dependencies {
  implementation(platform("io.grpc:grpc-bom:1.36.0"))

  implementation(project(":doorknob-core"))
  implementation(project(":doorknob-proto"))

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.session:spring-session-core")
  developmentOnly("org.springframework.boot:spring-boot-devtools")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("sh.ory.hydra:hydra-client:${Versions.HYDRA}")
  implementation("com.github.joschi.jackson:jackson-datatype-threetenbp:2.12.2")

  implementation("io.grpc:grpc-protobuf")
  implementation("io.grpc:grpc-stub")
  runtimeOnly("io.grpc:grpc-netty-shaded")
}

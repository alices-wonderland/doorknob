import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
  id("com.google.protobuf") version "0.8.15"
}

object Versions {

  const val PROTOC = "3.15.3"

  const val JAVAX_ANNOTATION = "1.3.2"

  const val GRPC = "1.36.0"
}

dependencies {
  api("javax.annotation:javax.annotation-api:${Versions.JAVAX_ANNOTATION}")
  api("com.google.protobuf:protobuf-java-util:${Versions.PROTOC}")

  implementation("io.grpc:grpc-netty-shaded:${Versions.GRPC}")
  implementation("io.grpc:grpc-protobuf:${Versions.GRPC}")
  implementation("io.grpc:grpc-stub:${Versions.GRPC}")
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:${Versions.PROTOC}"
  }
  plugins {
    id("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:${Versions.GRPC}"
    }
  }
  generateProtoTasks {
    all().forEach {
      it.plugins {
        id("grpc")
      }
    }
  }
}

idea {
  module {
    generatedSourceDirs.addAll(
      listOf(
        file("${protobuf.protobuf.generatedFilesBaseDir}/main/grpc"),
        file("${protobuf.protobuf.generatedFilesBaseDir}/main/java"),
      )
    )
  }
}

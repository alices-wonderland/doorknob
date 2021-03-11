package com.ukonnra.wonderland.doorknob.authentication.external.impl

import com.google.protobuf.StringValue
import com.ukonnra.wonderland.doorknob.authentication.DoorKnobProperties
import com.ukonnra.wonderland.doorknob.authentication.external.DoorKnobUserExternal
import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.doorknob.core.domain.user.UserAggregate
import com.ukonnra.wonderland.doorknob.proto.GetByIdentifierInputProto
import com.ukonnra.wonderland.doorknob.proto.IdentifierTypeProto
import com.ukonnra.wonderland.doorknob.proto.UserEndpointGrpc
import com.ukonnra.wonderland.doorknob.proto.toModel
import com.ukonnra.wonderland.infrastructure.toMono
import io.grpc.ManagedChannelBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.concurrent.Executors

@Component
class DoorKnobUserExternalGrpc @Autowired constructor(props: DoorKnobProperties) : DoorKnobUserExternal {
  private val executor = Executors.newWorkStealingPool()
  private val stub: UserEndpointGrpc.UserEndpointFutureStub

  init {
    val channel = ManagedChannelBuilder.forTarget(props.userEndpointUrl).usePlaintext().build()
    this.stub = UserEndpointGrpc.newFutureStub(channel)
  }

  override fun getByIdentifier(identifier: Identifier): Mono<UserAggregate?> {
    return this.stub.getByIdentifier(
      GetByIdentifierInputProto.newBuilder()
        .setType(IdentifierTypeProto.valueOf(identifier.type.name))
        .setValue(identifier.value).build()
    )
      .toMono(executor).map {
        it?.toModel()
      }
  }

  override fun getById(id: String): Mono<UserAggregate?> {
    return this.stub.getById(StringValue.of(id)).toMono(executor).map {
      it?.toModel()
    }
  }
}

package com.ukonnra.wonderland.doorknob.authentication.external.impl

import com.google.protobuf.StringValue
import com.ukonnra.wonderland.doorknob.authentication.DoorKnobProperties
import com.ukonnra.wonderland.doorknob.authentication.DoorKnobUserModel
import com.ukonnra.wonderland.doorknob.authentication.external.DoorKnobUserExternal
import com.ukonnra.wonderland.doorknob.proto.GetByIdentifierInput
import com.ukonnra.wonderland.doorknob.proto.UserEndpointGrpc
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

  override fun getByIdentifier(type: Int, identifier: String): Mono<DoorKnobUserModel?> {
    return this.stub.getByIdentifier(GetByIdentifierInput.newBuilder().setTypeValue(type).setValue(identifier).build())
      .toMono(executor).map {
        it?.let {
          DoorKnobUserModel(it.id, it.password)
        }
      }
  }

  override fun getById(id: String): Mono<DoorKnobUserModel?> {
    return this.stub.getById(StringValue.of(id)).toMono(executor).map {
      it?.let {
        DoorKnobUserModel(it.id, it.password)
      }
    }
  }
}

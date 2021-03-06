package com.ukonnra.wonderland.doorknob.authentication.external

import com.ukonnra.wonderland.doorknob.authentication.DoorKnobUserModel
import reactor.core.publisher.Mono

interface DoorKnobUserExternal {
  fun getByIdentifier(type: Int, identifier: String): Mono<DoorKnobUserModel?>
  fun getById(id: String): Mono<DoorKnobUserModel?>
}

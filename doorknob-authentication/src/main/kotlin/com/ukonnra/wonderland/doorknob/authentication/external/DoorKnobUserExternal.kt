package com.ukonnra.wonderland.doorknob.authentication.external

import com.ukonnra.wonderland.doorknob.authentication.DoorKnobUserModel
import reactor.core.publisher.Mono

interface DoorKnobUserExternal {
  fun getByIdentifier(identifier: String): Mono<DoorKnobUserModel?>
}

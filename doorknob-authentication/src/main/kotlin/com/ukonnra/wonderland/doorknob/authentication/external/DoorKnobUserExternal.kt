package com.ukonnra.wonderland.doorknob.authentication.external

import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.doorknob.core.domain.user.UserAggregate
import reactor.core.publisher.Mono

interface DoorKnobUserExternal {
  fun getByIdentifier(identifier: Identifier): Mono<UserAggregate?>
  fun getById(id: String): Mono<UserAggregate?>
}

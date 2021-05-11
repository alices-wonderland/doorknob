package com.ukonnra.wonderland.doorknob.core.domain.user

import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class IdentifierActivator {
  suspend fun activate(identType: Identifier.Type, identValue: String) {
    delay(500)
    println("Start activate Identifier[$identType - $identValue] using Way[${identType.defaultWay()}]")
  }

  suspend fun activate(identifier: Identifier) {
  }
}

package com.ukonnra.wonderland.infrastructure.core.service

import com.ukonnra.wonderland.infrastructure.core.Aggregate
import com.ukonnra.wonderland.infrastructure.core.Command
import com.ukonnra.wonderland.infrastructure.core.error.AbstractError
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

interface AbstractTransactionService : AbstractService {
  @Throws(AbstractError::class)
  suspend fun handleCommands(authToken: OAuth2AuthenticatedPrincipal, commands: List<Command<*, *>>): List<Aggregate<*>>
}

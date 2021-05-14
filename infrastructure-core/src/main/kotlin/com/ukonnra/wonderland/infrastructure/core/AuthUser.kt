package com.ukonnra.wonderland.infrastructure.core

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

abstract class AuthUser<T : Aggregate<*>, S : AuthScope>(private val authToken: OAuth2AuthenticatedPrincipal) {
  abstract val user: T?

  val id: AggregateId?
    get() = user?.id

  val isSuperUser: Boolean = authToken.name == authToken.getAttribute<String>("client_id")

  fun hasScope(scope: S): Boolean = isSuperUser || authToken.authorities.any { it.authority == "SCOPE_${scope.value}" }
}

interface AuthScope {
  val value: String
}

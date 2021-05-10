package com.ukonnra.wonderland.infrastructure.core

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

abstract class AuthUser<T : Aggregate<*>>(private val authToken: OAuth2AuthenticatedPrincipal) {
  abstract val user: T?

  val id: AggregateId?
    get() = user?.id

  val isSuperUser: Boolean = authToken.name == authToken.getAttribute<String>("client_id")

  fun hasScope(scope: String): Boolean = isSuperUser || authToken.authorities.any { it.authority == "SCOPE_$scope" }
}

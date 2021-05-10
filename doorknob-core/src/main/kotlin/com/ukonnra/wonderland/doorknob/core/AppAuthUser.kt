package com.ukonnra.wonderland.doorknob.core

import com.ukonnra.wonderland.doorknob.core.domain.user.Role
import com.ukonnra.wonderland.doorknob.core.domain.user.UserAggregate
import com.ukonnra.wonderland.infrastructure.core.AuthUser
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

data class AppAuthUser(
  val authToken: OAuth2AuthenticatedPrincipal,
  override val user: UserAggregate?,
) : AuthUser<UserAggregate>(authToken) {
  fun hasRole(role: Role, strictlyHigher: Boolean = false): Boolean =
    isSuperUser || (
      (user?.data as? UserAggregate.Data.Created)?.role?.let {
        if (strictlyHigher) {
          it > role
        } else {
          it >= role
        }
      } ?: false
      )
}

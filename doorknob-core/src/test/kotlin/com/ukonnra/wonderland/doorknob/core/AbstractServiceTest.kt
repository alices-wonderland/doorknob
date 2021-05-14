package com.ukonnra.wonderland.doorknob.core

import com.ukonnra.wonderland.doorknob.core.domain.user.UserAggregate
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal

interface AbstractServiceTest {
  val superAuthUser: AppAuthUser
    get() = AppAuthUser(
      OAuth2IntrospectionAuthenticatedPrincipal(
        "test-client",
        mapOf("client_id" to "test-client"),
        emptyList()
      )
    )

  fun getAuthUser(user: UserAggregate, scope: List<AppAuthScope>) = AppAuthUser(
    OAuth2IntrospectionAuthenticatedPrincipal(
      user.id.value,
      mapOf("sub" to user.id.value),
      scope.map { SimpleGrantedAuthority("SCOPE_${it.value}") },
    ),
    user
  )
}

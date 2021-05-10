package com.ukonnra.wonderland.doorknob.core.domain.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.ukonnra.wonderland.infrastructure.core.Aggregate
import com.ukonnra.wonderland.infrastructure.core.AggregateCompanion
import com.ukonnra.wonderland.infrastructure.core.AggregateId
import com.ukonnra.wonderland.infrastructure.core.error.WonderlandError
import sh.ory.hydra.model.OAuth2Client
import java.util.UUID

sealed class ClientAggregate(
  open val name: String,
  open val scopes: Set<String>,
  open val meta: Set<MetaItem>
) : Aggregate<ClientAggregate.Id> {
  data class Id(override val value: String) : AggregateId {
    constructor() : this(UUID.randomUUID().toString())
  }

  override val companion: AggregateCompanion
    get() = ClientAggregate

  open fun toHydra(): OAuth2Client = OAuth2Client()
    .clientId(this.id.value)
    .clientName(this.name)
    .scope(this.scopes.joinToString(" "))
    .metadata(this.meta)

  companion object : AggregateCompanion {
    override val type: String
      get() = "DoorKnob:Client"

    fun from(client: OAuth2Client): ClientAggregate {
      if (client.clientId == null || client.clientName == null || client.scope == null) {
        throw WonderlandError.NotFound(type, client.clientId ?: "null")
      }

      val meta: Set<MetaItem> = jacksonObjectMapper().convertValue(client.metadata, jacksonTypeRef())

      return when {
        client.grantTypes == Frontend.GRANT_TYPES &&
          client.responseTypes == Frontend.RESPONSE_TYPES
          && client.tokenEndpointAuthMethod == Frontend.TOKEN_AUTH_METHOD
        -> Frontend(
          client.clientName!!,
          client.scope!!.split(" ").toSet(),
          client.redirectUris!!.toSet(),
          Id(client.clientId!!),
          meta
        )
        client.grantTypes == Backend.GRANT_TYPES &&
          client.responseTypes == Backend.RESPONSE_TYPES
          && client.clientSecret != null
        -> Backend(
          client.clientName!!,
          client.scope!!.split(" ").toSet(),
          client.clientSecret!!,
          Id(client.clientId!!),
          meta
        )
        else -> throw WonderlandError.NotFound(type, client.clientId!!)
      }
    }
  }

  data class Frontend(
    override val name: String,
    override val scopes: Set<String>,
    val redirectUris: Set<String>,
    override val id: Id = Id(),
    override val meta: Set<MetaItem> = emptySet(),
  ) : ClientAggregate(name, scopes, meta) {
    companion object {
      internal val GRANT_TYPES = setOf("authorization_code", "refresh_token")
      internal val RESPONSE_TYPES = setOf("code", "token", "id_token")
      internal const val TOKEN_AUTH_METHOD = "none"
    }

    override fun toHydra(): OAuth2Client = super.toHydra()
      .redirectUris(this.redirectUris.toList())
      .grantTypes(GRANT_TYPES.toList())
      .responseTypes(RESPONSE_TYPES.toList())
      .tokenEndpointAuthMethod("none")
  }

  data class Backend(
    override val name: String,
    override val scopes: Set<String>,
    val secret: String,
    override val id: Id = Id(),
    override val meta: Set<MetaItem> = emptySet(),
  ) : ClientAggregate(name, scopes, meta) {
    companion object {
      internal val GRANT_TYPES = setOf("client_credentials")
      internal val RESPONSE_TYPES = setOf("code")
    }

    override fun toHydra(): OAuth2Client = super.toHydra()
      .clientSecret(this.secret)
      .grantTypes(GRANT_TYPES.toList())
      .responseTypes(RESPONSE_TYPES.toList())
  }

  enum class MetaItem {
    SKIP_CONSENT;
  }
}

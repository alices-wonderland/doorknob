package com.ukonnra.wonderland.doorknob.core.domain.client

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.ukonnra.wonderland.annotations.AggregateRoot
import com.ukonnra.wonderland.infrastructure.core.error.WonderlandError
import sh.ory.hydra.model.OAuth2Client

@AggregateRoot(service = "DoorKnob")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
  JsonSubTypes.Type(value = Client.Frontend::class, name = "Frontend"),
  JsonSubTypes.Type(value = Client.Backend::class, name = "Backend")
)
sealed class Client(open val id: ClientId, open val name: String, open val scopes: Set<String>, open val meta: Meta) {
  open fun toHydra(): OAuth2Client = OAuth2Client()
    .clientId(this.id.toString())
    .clientName(this.name)
    .scope(this.scopes.joinToString(" "))
    .metadata(this.meta)

  companion object {
    fun from(client: OAuth2Client): Client {
      if (client.clientId == null || client.clientName == null || client.scope == null) {
        throw WonderlandError.NotFound(ClientAggregate.type, client.clientId ?: "null")
      }

      val meta: Meta = jacksonObjectMapper().convertValue(client.metadata, jacksonTypeRef())

      return when {
        client.grantTypes == Frontend.GRANT_TYPES &&
          client.responseTypes == Frontend.RESPONSE_TYPES
          && client.tokenEndpointAuthMethod == Frontend.TOKEN_AUTH_METHOD
        -> Frontend(
          client.clientName!!,
          client.scope!!.split(" ").toSet(),
          client.redirectUris!!.toSet(),
          ClientId(client.clientId!!),
          meta
        )
        client.grantTypes == Backend.GRANT_TYPES &&
          client.responseTypes == Backend.RESPONSE_TYPES
          && client.clientSecret != null
        -> Backend(
          client.clientName!!,
          client.scope!!.split(" ").toSet(),
          client.clientSecret!!,
          ClientId(client.clientId!!),
          meta
        )
        else -> throw WonderlandError.NotFound(ClientAggregate.type, client.clientId!!)
      }
    }
  }

  data class Frontend(
    override val name: String,
    override val scopes: Set<String>,
    val redirectUris: Set<String>,
    override val id: ClientId = ClientId(),
    override val meta: Meta = Meta(),
  ) : Client(id, name, scopes, meta) {
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
    override val id: ClientId = ClientId(),
    override val meta: Meta = Meta(),
  ) : Client(id, name, scopes, meta) {
    companion object {
      internal val GRANT_TYPES = setOf("client_credentials")
      internal val RESPONSE_TYPES = setOf("code")
    }

    override fun toHydra(): OAuth2Client = super.toHydra()
      .clientSecret(this.secret)
      .grantTypes(GRANT_TYPES.toList())
      .responseTypes(RESPONSE_TYPES.toList())
  }

  data class Meta(val skipConsent: Boolean = false)
}

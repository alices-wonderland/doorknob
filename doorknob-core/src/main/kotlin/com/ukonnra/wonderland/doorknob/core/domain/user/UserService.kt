package com.ukonnra.wonderland.doorknob.core.domain.user

import com.ukonnra.wonderland.doorknob.core.Errors
import com.ukonnra.wonderland.infrastructure.core.error.WonderlandError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserService @Autowired constructor(
  private val userRepository: UserRepository,
  private val passwordEncoder: PasswordEncoder
) {
  private fun checkExisting(aggregate: UserAggregate?, identValue: String): UserAggregate =
    aggregate ?: throw WonderlandError.NotFound(UserAggregate.type, identValue)

  fun login(identType: Identifier.Type, identValue: String): Mono<UserId> =
    userRepository.getByIdentifier(identType, identValue).map { checkExisting(it, identValue) }.map { (value) ->
      val identifier =
        value.identifiers.find { it.type == identType && it.value == identValue && it.activated }
          ?: throw Errors.IdentifierNotActivated(value.id, identValue)

      if (!passwordEncoder.matches(identifier.value, value.password)) {
        throw WonderlandError.NoAuth(value.id.toString())
      }

      value.id
    }

  fun consent(userId: String): Mono<Unit> = userRepository.getById(UserId(userId)).map { checkExisting(it, userId) }
}

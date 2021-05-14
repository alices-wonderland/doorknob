package com.ukonnra.wonderland.doorknob.core.domain.user

import com.ukonnra.wonderland.doorknob.core.AbstractServiceTest
import com.ukonnra.wonderland.doorknob.core.AppAuthScope
import com.ukonnra.wonderland.doorknob.core.AppAuthUser
import com.ukonnra.wonderland.doorknob.core.USERS
import com.ukonnra.wonderland.infrastructure.core.error.WonderlandError
import com.ukonnra.wonderland.infrastructure.testsuite.TestTask
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

internal typealias TaskSuccess<C> = TestTask.Success<AppAuthUser, C, UserAggregate>
internal typealias TaskFailure<E> = TestTask.Failure<AppAuthUser, UserCommand, E>

abstract class AbstractUserServiceTest : AbstractServiceTest {
  abstract val userService: UserService
  abstract val userRepository: UserRepository

  @BeforeEach
  fun beforeEach() = runBlocking {
    userRepository.saveAll(USERS)
  }

  @AfterEach
  fun afterEach() = runBlocking {
    userRepository.clear()
  }

  abstract suspend fun <C : UserCommand> doTest(task: TaskSuccess<C>)

  abstract suspend fun <E : Throwable> doTest(task: TaskFailure<E>)

  @Test
  fun testSuperCreate_Normal() = runBlocking {

    fun checker(command: UserCommand.SuperCreate, aggregate: UserAggregate) {
      val userInfo = aggregate.userInfo as UserAggregate.UserInfo.Created
      Assertions.assertEquals(command.nickname, userInfo.nickname)
      Assertions.assertEquals(command.password, userInfo.password.value)
      Assertions.assertEquals(command.identifier, userInfo.identifiers.values.first())
    }

    listOf(
      TaskSuccess(
        superAuthUser,
        UserCommand.SuperCreate(
          "nickname",
          "password",
          Identifier.Activated(Identifier.Type.EMAIL, "new1@email.com"),
          Role.OWNER
        ),
        ::checker
      ),
      TaskSuccess(
        getAuthUser(USERS[2], listOf(AppAuthScope.USERS_WRITE)),
        UserCommand.SuperCreate(
          "nickname",
          "password",
          Identifier.Activated(Identifier.Type.EMAIL, "new2@email.com"),
          Role.ADMIN
        ),
        ::checker
      )
    ).forEach {
      doTest(it)
    }
  }

  @Test
  fun testSuperCreate_NoAuthInvalidToken() = runBlocking {
    val command = UserCommand.SuperCreate(
      "nickname1",
      "password",
      Identifier.Activated(Identifier.Type.EMAIL, "email@email.com"),
      Role.ADMIN
    )
    listOf(
      WonderlandError.NoAuth::class to getAuthUser(USERS[0], listOf(AppAuthScope.USERS_WRITE)),
      WonderlandError.NoAuth::class to getAuthUser(USERS[1], listOf(AppAuthScope.USERS_WRITE)),
      WonderlandError.InvalidToken::class to getAuthUser(USERS[2], listOf(AppAuthScope.USERS_READ)),
    ).map { (ex, authUser) ->
      TaskFailure(authUser, command, ex)
    }.forEach {
      doTest(it)
    }
  }

  @Test
  fun testSuperCreate_AlreadyExists() = runBlocking {
    TaskFailure(
      superAuthUser,
      UserCommand.SuperCreate(
        "nickname1",
        "password",
        Identifier.Activated(Identifier.Type.EMAIL, "user1@email.com"),
        Role.ADMIN
      ),
      WonderlandError.AlreadyExists::class
    )
      .let {
        doTest(it)
      }
  }

  @Test
  fun testStartCreate_Normal() = runBlocking {
    TaskSuccess(
      null,
      UserCommand.StartCreate(Identifier.Type.EMAIL, "new@email.com")
    ) { command, aggregate ->
      val userInfo = aggregate.userInfo as UserAggregate.UserInfo.Uncreated
      Assertions.assertEquals(command.identType, userInfo.identifier.type)
      Assertions.assertEquals(command.identValue, userInfo.identifier.value)
    }
      .let { doTest(it) }
  }

  @Test
  fun testRefreshCreate_Normal() = runBlocking {
    val code = USERS[3].let {
      val userInfo = it.userInfo as UserAggregate.UserInfo.Uncreated
      userRepository.save(
        it.copy(
          userInfo.copy(
            userInfo.identifier.copy(
              createAt = Instant.now().minusSeconds(1_000)
            )
          )
        )
      )
      userInfo.identifier.code
    }

    TaskSuccess(
      getAuthUser(USERS[3], listOf(AppAuthScope.USERS_WRITE)),
      UserCommand.RefreshCreate(USERS[3].id),
    ) { _, aggregate ->
      val userInfo = aggregate.userInfo as UserAggregate.UserInfo.Uncreated
      Assertions.assertNotEquals(code, userInfo.identifier.code)
    }
      .let { doTest(it) }
  }
}

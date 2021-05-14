package com.ukonnra.wonderland.doorknob.core.domain.user.impl

import com.ukonnra.wonderland.doorknob.core.TestApplicationConfiguration
import com.ukonnra.wonderland.doorknob.core.domain.user.AbstractUserServiceTest
import com.ukonnra.wonderland.doorknob.core.domain.user.TaskFailure
import com.ukonnra.wonderland.doorknob.core.domain.user.TaskSuccess
import com.ukonnra.wonderland.doorknob.core.domain.user.UserCommand
import com.ukonnra.wonderland.doorknob.core.domain.user.UserRepository
import com.ukonnra.wonderland.doorknob.core.domain.user.UserService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@ContextConfiguration(classes = [TestApplicationConfiguration::class])
@TestPropertySource("classpath:redisson-lock.test.properties")
class DirectUserServiceTest @Autowired constructor(
  override val userService: UserService,
  override val userRepository: UserRepository,
) : AbstractUserServiceTest() {
  override suspend fun <C : UserCommand> doTest(task: TaskSuccess<C>) {
    val result = userService.handle(task.authUser, task.command)
    task.checker(task.command, result)
    Assertions.assertEquals(result, userRepository.getById(result.id))
  }

  override suspend fun <E : Throwable> doTest(task: TaskFailure<E>) {
    val result = Assertions.assertThrows(task.ex.java) {
      runBlocking { userService.handle(task.authUser, task.command) }
    }
    task.checker?.invoke(result)
  }
}

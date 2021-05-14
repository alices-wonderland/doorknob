package com.ukonnra.wonderland.doorknob.core

import com.ukonnra.wonderland.doorknob.core.domain.user.Identifier
import com.ukonnra.wonderland.doorknob.core.domain.user.Role
import com.ukonnra.wonderland.doorknob.core.domain.user.UserAggregate

val USERS = listOf(
  UserAggregate(
    UserAggregate.UserInfo.Created(
      "user1",
      UserAggregate.Password.Normal("password"),
      listOf(
        Identifier.Activated(Identifier.Type.EMAIL, "user1@email.com"),
        Identifier.Hanging(Identifier.Type.PHONE, "phone_user1"),
      ).associateBy { it.type },
      Role.USER,
    )
  ),
  UserAggregate(
    UserAggregate.UserInfo.Created(
      "admin1",
      UserAggregate.Password.Normal("password"),
      listOf(
        Identifier.Hanging(Identifier.Type.PHONE, "phone_admin1"),
      ).associateBy { it.type },
      Role.ADMIN,
    )
  ),
  UserAggregate(
    UserAggregate.UserInfo.Created(
      "owner1",
      UserAggregate.Password.Normal("password"),
      listOf(
        Identifier.Hanging(Identifier.Type.PHONE, "phone_owner1"),
      ).associateBy { it.type },
      Role.OWNER,
    )
  ),
  UserAggregate.uncreated(Identifier.Type.EMAIL, "uncreated1"),
)

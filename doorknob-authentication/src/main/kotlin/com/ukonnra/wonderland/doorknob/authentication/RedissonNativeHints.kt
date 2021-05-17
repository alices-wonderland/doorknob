package com.ukonnra.wonderland.doorknob.authentication

import io.netty.channel.socket.nio.NioDatagramChannel
import org.redisson.api.RedissonClient
import org.redisson.config.BaseConfig
import org.redisson.config.Config
import org.redisson.config.SingleServerConfig
import org.springframework.context.annotation.Configuration
import org.springframework.nativex.hint.AccessBits
import org.springframework.nativex.hint.NativeHint
import org.springframework.nativex.hint.TypeHint
import org.springframework.nativex.type.NativeConfiguration

@Configuration
@NativeHint(
  trigger = RedissonClient::class,
  types = [
    TypeHint(types = [NioDatagramChannel::class], access = AccessBits.DECLARED_CONSTRUCTORS),
    TypeHint(
      types = [
        BaseConfig::class,
        Config::class,
        SingleServerConfig::class,
      ],
      access = AccessBits.FULL_REFLECTION
    ),
  ]
)
class RedissonNativeHints : NativeConfiguration

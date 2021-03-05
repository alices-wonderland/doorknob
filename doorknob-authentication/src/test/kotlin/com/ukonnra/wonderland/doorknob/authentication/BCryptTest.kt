package com.ukonnra.wonderland.doorknob.authentication

import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class BCryptTest {
  @Test
  fun testEncode() {
    val encoder = BCryptPasswordEncoder()
    val string = "hello"
    val encoded = encoder.encode(string)
    println(encoded)

    val encoder2 = BCryptPasswordEncoder()
    val encoded2 = encoder2.matches(string, encoded)
    println(encoded2)
  }
}

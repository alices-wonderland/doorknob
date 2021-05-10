package com.ukonnra.wonderland.infrastructure.testsuite

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions

interface SerdeTester {
  val mapper: ObjectMapper
}

inline fun <reified T> SerdeTester.check(value: T, expected: String) {
  val jsonMap = mapper.convertValue(value, jacksonTypeRef<Map<String, Any>>())
  val expectedMap = mapper.readValue(expected, jacksonTypeRef<Map<String, Any>>())
  Assertions.assertEquals(expectedMap, jsonMap)

  val obj: T = mapper.convertValue(jsonMap, jacksonTypeRef())
  Assertions.assertEquals(value, obj)
}

package com.ukonnra.wonderland.doorknob.authentication

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class Application

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
  runApplication<Application>(*args)
}

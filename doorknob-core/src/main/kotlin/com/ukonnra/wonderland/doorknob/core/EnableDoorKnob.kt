package com.ukonnra.wonderland.doorknob.core

import com.ukonnra.wonderland.doorknob.core.domain.user.UserService
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(DoorKnobConfiguration::class)
@ComponentScan(basePackageClasses = [UserService::class])
annotation class EnableDoorKnob

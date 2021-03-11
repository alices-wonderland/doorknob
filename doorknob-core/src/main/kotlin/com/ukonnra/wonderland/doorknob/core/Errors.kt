package com.ukonnra.wonderland.doorknob.core

import com.ukonnra.wonderland.infrastructure.error.AbstractError

sealed class Errors(override val message: String) : AbstractError(message)

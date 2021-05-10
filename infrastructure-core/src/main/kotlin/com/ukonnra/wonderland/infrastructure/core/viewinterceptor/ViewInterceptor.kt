package com.ukonnra.wonderland.infrastructure.core.viewinterceptor

import com.ukonnra.wonderland.infrastructure.core.Filter

interface ViewInterceptor {
  suspend fun getAll(ids: List<String>, filter: Filter): List<String>
}

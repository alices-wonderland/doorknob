package com.ukonnra.wonderland.infrastructure.core.cache

import com.ukonnra.wonderland.infrastructure.core.Aggregate
import com.ukonnra.wonderland.infrastructure.core.AggregateId

interface Cache<ID : AggregateId, A : Aggregate<ID>>

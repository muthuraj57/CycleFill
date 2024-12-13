package com.muthuraj.cycle.fill.models

data class Collection(
    val id: Int,
    val name: String,
    val documentPath: String,
    val lastRefillDate: String?,
    val daysElapsed: Pair<Int, String>?
)
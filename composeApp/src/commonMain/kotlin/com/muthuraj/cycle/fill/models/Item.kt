package com.muthuraj.cycle.fill.models

data class Item(
    val name: String,
    val imageUrl: String,
    val documentPath: String
)

data class ItemCollection(
    val id: Int,
    val name: String,
    val documentPath: String,
    val lastRefillDate: String?,
    val daysElapsed: Pair<Int, String>?
)
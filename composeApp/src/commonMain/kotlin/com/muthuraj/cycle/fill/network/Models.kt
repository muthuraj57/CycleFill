/* $Id$ */
package com.muthuraj.cycle.fill.network

import kotlinx.serialization.Serializable

/**
 * Created by Muthuraj on 14/12/24.
 */
@Serializable
data class Response<Type : Any>(val success: Boolean, val data: List<Type>?, val message: String?)

@Serializable
data class CategoryResponse(val id: Int, val name: String, val icon: String)

@Serializable
data class SubCategoryResponse(val id: Int, val name: String, val icon: String, val categoryId: Int)

@Serializable
data class CollectionResponse(
    val id: Int,
    val name: String,
    val subCategoryId: Int,
    val lastDate: String?,
    val totalAmount: Long?
)

@Serializable
data class ItemResponse(
    val id: Int,
    val date: String,
    val description: String,
    val collectionId: Int
)

@Serializable
data class ItemDetailedResponse(
    val id: Int,
    val date: String,
    val description: String,
    val collectionId: Int,
    val collection_name: String,
    val subcategory_name: String,
    val category_name: String
)

@Serializable
data class PostResponse(val id: Int?, val success: Boolean, val message: String)
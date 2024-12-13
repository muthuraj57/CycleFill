/* $Id$ */
package com.muthuraj.cycle.fill.navigation

import kotlinx.serialization.Serializable

/**
 * Created by Muthuraj on 07/12/24.
 */
sealed class Screen {

    @Serializable
    data class Dashboard(val type: Type, val categoryId: Int? = null, val categoryName: String? = null) :
        Screen() {

        enum class Type {
            Category, SubCategory
        }
    }

    @Serializable
    data object Recents : Screen()

    @Serializable
    data class ItemDetail(
        val subCategoryId: Int,
        val itemName: String
    ) : Screen()

    @Serializable
    data class CollectionDetail(
        val collectionId: Int,
        val collectionName: String
    ) : Screen()
}
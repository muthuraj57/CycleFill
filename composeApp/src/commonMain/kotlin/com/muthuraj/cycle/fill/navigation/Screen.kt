/* $Id$ */
package com.muthuraj.cycle.fill.navigation

import kotlinx.serialization.Serializable

/**
 * Created by Muthuraj on 07/12/24.
 */
@Serializable
sealed class Screen {

    @Serializable
    data class Dashboard(val type: Int, val categoryId: Int? = null, val categoryName: String? = null) :
        Screen() {

        object Type {
            const val Category = 1
            const val SubCategory = 2
        }
    }

    @Serializable
    data object Recents : Screen()

    @Serializable
    data class Collections(
        val subCategoryId: Int,
        val itemName: String
    ) : Screen()

    @Serializable
    data class Items(
        val collectionId: Int,
        val collectionName: String
    ) : Screen()
}
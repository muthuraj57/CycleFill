/* $Id$ */
package com.muthuraj.cycle.fill.navigation

import kotlinx.serialization.Serializable

/**
 * Created by Muthuraj on 07/12/24.
 */
sealed class Screen {

    @Serializable
    data object Dashboard : Screen()

    @Serializable
    data object Recents : Screen()

    @Serializable
    data class ItemDetail(
        val documentPath: String,
        val itemName: String
    ) : Screen()

    @Serializable
    data class CollectionDetail(
        val documentPath: String,
        val collectionName: String
    ) : Screen()
}
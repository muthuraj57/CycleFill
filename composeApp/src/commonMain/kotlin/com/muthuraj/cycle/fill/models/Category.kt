package com.muthuraj.cycle.fill.models

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable

/**
 * Created by Muthuraj on 08/12/24.
 */
data class Category(val name: String, val imageUrl: String, val documentPath: String)
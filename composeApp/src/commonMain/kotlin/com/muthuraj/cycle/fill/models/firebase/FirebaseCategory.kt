package com.muthuraj.cycle.fill.models.firebase

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable

@Serializable
data class FirebaseCategory(val name: String, val imagePath: String, val rinse_aid: List<Timestamp>?)
package co.ke.xently.shopping.libraries.data.source.remote.models

import co.ke.xently.shopping.libraries.data.source.Coordinate

data class RecommendationRequestResource(
    val items: List<Any>,
    // Save shopping list items...
    val persist: Boolean,
    val myLocation: Coordinate?,
    val shopDistanceMeters: Int,
    val cacheRecommendationsForLater: Boolean,
)
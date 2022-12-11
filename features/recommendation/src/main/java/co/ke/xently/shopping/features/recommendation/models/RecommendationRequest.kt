package co.ke.xently.shopping.features.recommendation.models

import co.ke.xently.shopping.libraries.data.source.Coordinate

internal data class RecommendationRequest(
    val items: List<Any>,
    // Save shopping list items...
    val persist: Boolean = false,
    val shopDistanceMeters: Int = 0,
    val myLocation: Coordinate? = null,
    val cacheRecommendationsForLater: Boolean = false,
)
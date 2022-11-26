package co.ke.xently.shopping.libraries.data.source.remote.models

import co.ke.xently.shopping.libraries.data.source.Coordinate

data class RecommendationRequestResource(
    val items: List<Any>,
    // Save shopping list items...
    val persist: Boolean,
    val lookupId: String?,
    val myLocation: Coordinate?,
    val isLocationPermissionGranted: Boolean,
    val cacheRecommendationsForLater: Boolean,
)
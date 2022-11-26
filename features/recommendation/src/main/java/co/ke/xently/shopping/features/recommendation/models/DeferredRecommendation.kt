package co.ke.xently.shopping.features.recommendation.models

internal data class DeferredRecommendation(val id: String, val numberOfItems: Int = 0) {
    override fun toString(): String {
        return id
    }
}

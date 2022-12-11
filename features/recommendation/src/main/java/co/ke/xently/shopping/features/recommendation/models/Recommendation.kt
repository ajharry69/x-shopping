package co.ke.xently.shopping.features.recommendation.models

import co.ke.xently.shopping.libraries.data.source.Shop

internal data class Recommendation(
    val shop: Shop,
    val hit: Hit,
    val miss: Miss,
    val expenditure: Expenditure,
) {
    val numberOfItems: Int get() = hit.count + miss.count

    data class Hit(val items: List<Item>, val count: Int) {
        data class Item(
            val found: String,
            val unitPrice: Float,
            val requested: String,
            val purchaseQuantity: Int = 1,
        )
    }

    data class Miss(val items: List<String>, val count: Int)
    data class Expenditure(val unit: Float, val total: Float)

    companion object{
        val DEFAULT = Recommendation(
            shop = Shop.DEFAULT_INSTANCE,
            hit = Hit(
                items = listOf(
                    Hit.Item(
                        found = "Bread",
                        unitPrice = 50f,
                        requested = "Superloaf bread",
                    ),
                ),
                count = 1,
            ),
            miss = Miss(
                items = listOf(
                    "Blue band",
                ),
                count = 1,
            ),
            expenditure = Expenditure(
                unit = 50f,
                total = 50f,
            ),
        )
    }
}
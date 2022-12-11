package co.ke.xently.shopping.libraries.data.source.remote.models

import co.ke.xently.shopping.libraries.data.source.Shop

data class RecommendationResource(
    val shop: Shop,
    val hit: Hit,
    val miss: Miss,
    val expenditure: Expenditure,
) {
    data class Hit(val items: List<Item>, val count: Int) {
        data class Item(
            val found: String,
            val unitPrice: Float,
            val requested: String,
            val purchaseQuantity: Int,
        )
    }

    data class Miss(val items: List<String>, val count: Int)
    data class Expenditure(val unit: Float, val total: Float)
}
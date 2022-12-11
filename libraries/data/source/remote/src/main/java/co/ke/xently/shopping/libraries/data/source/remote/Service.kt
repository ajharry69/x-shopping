package co.ke.xently.shopping.libraries.data.source.remote

import co.ke.xently.shopping.libraries.data.source.remote.services.*
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class Service @Inject constructor(val retrofit: Retrofit) {
    val account: AccountService = retrofit.create(AccountService::class.java)
    val shop: ShopService = retrofit.create(ShopService::class.java)
    val product: ProductService = retrofit.create(ProductService::class.java)
    val shoppingList: ShoppingListService = retrofit.create(ShoppingListService::class.java)
    val recommendation: RecommendationService = retrofit.create(RecommendationService::class.java)
}

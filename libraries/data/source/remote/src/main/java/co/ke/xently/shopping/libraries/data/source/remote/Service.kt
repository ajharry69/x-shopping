package co.ke.xently.shopping.libraries.data.source.remote

import co.ke.xently.shopping.libraries.data.source.remote.services.AccountService
import co.ke.xently.shopping.libraries.data.source.remote.services.ShopService
import co.ke.xently.shopping.libraries.data.source.remote.services.ShoppingListService
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class Service @Inject constructor(val retrofit: Retrofit) {
    val account: AccountService = retrofit.create(AccountService::class.java)
    val shop: ShopService = retrofit.create(ShopService::class.java)
    val shoppingList: ShoppingListService = retrofit.create(ShoppingListService::class.java)
}

package co.ke.xently.shopping.features.products

import com.ramcosta.composedestinations.navigation.DestinationsNavigator

interface ProductsNavigator : DestinationsNavigator {
    fun onAddNewShopClicked(name: String)
}
package co.ke.xently.shopping.features.shoppinglist

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph(start = true)
@NavGraph
annotation class ShoppingListNavGraph(val start: Boolean = false)

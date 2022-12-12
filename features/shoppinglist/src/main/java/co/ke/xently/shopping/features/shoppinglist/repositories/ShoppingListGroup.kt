package co.ke.xently.shopping.features.shoppinglist.repositories

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import co.ke.xently.shopping.features.shoppinglist.GroupBy

@Parcelize
data class ShoppingListGroup(
    val group: String,
    val groupBy: GroupBy = GroupBy.DateAdded,
) : Parcelable
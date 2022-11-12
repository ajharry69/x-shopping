package co.ke.xently.shopping.features.products.models.mappers

import co.ke.xently.shopping.features.products.models.Product
import co.ke.xently.shopping.libraries.data.source.Shop
import co.ke.xently.shopping.libraries.data.source.local.models.ProductEntity
import co.ke.xently.shopping.libraries.data.source.remote.models.ProductResource


val ProductEntity.asUIInstance
    get() = Product(
        id = id,
        name = name,
        unit = unit,
        shop = Shop.DEFAULT_INSTANCE,
        unitQuantity = unitQuantity,
        unitPrice = unitPrice,
        brands = emptyList(),
        attributes = emptyList(),
        purchasedQuantity = purchasedQuantity,
        datePurchased = datePurchased,
    )


val ProductEntity.WithRelated.asUIInstance
    get() = entity.asUIInstance.copy(
        shop = shop.asUIInstance,
        brands = brands.map {
            Product.Brand(name = it.name)
        },
        attributes = attributes.map {
            Product.Attribute(
                name = it.name,
                value = it.value,
                values = emptyList(),
            )
        },
    )

val ProductResource.asEntity
    get() = ProductEntity(
        id = id,
        name = name,
        unit = unit,
        unitPrice = unitPrice,
        shopId = shop,
        unitQuantity = unitQuantity,
        purchasedQuantity = purchasedQuantity,
        datePurchased = datePurchased,
    )
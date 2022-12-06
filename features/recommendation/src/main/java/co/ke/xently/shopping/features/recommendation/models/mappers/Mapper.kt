package co.ke.xently.shopping.features.recommendation.models.mappers

import co.ke.xently.shopping.features.recommendation.models.Recommendation
import co.ke.xently.shopping.features.recommendation.models.RecommendationRequest
import co.ke.xently.shopping.libraries.data.source.remote.models.RecommendationRequestResource
import co.ke.xently.shopping.libraries.data.source.remote.models.RecommendationResource

internal val RecommendationRequest.asResource
    get() = RecommendationRequestResource(
        items = items,
        persist = persist,
        lookupId = lookupId,
        myLocation = myLocation,
        isLocationPermissionGranted = isLocationPermissionGranted,
        cacheRecommendationsForLater = cacheRecommendationsForLater,
    )

internal val RecommendationRequestResource.asUi
    get() = RecommendationRequest(
        items = items,
        persist = persist,
        lookupId = lookupId,
        myLocation = myLocation,
        isLocationPermissionGranted = isLocationPermissionGranted,
        cacheRecommendationsForLater = cacheRecommendationsForLater,
    )

internal val Recommendation.asResource
    get() = RecommendationResource(
        shop = shop,
        hit = hit.run {
            RecommendationResource.Hit(
                count = count,
                items = items.map {
                    RecommendationResource.Hit.Item(
                        found = it.found,
                        unitPrice = it.unitPrice,
                        requested = it.requested,
                        purchaseQuantity = it.purchaseQuantity,
                    )
                },
            )
        },
        miss = miss.run {
            RecommendationResource.Miss(items = items, count = count)
        },
        expenditure = expenditure.run {
            RecommendationResource.Expenditure(unit = unit, total = total)
        },
    )

internal val RecommendationResource.asUi
    get() = Recommendation(
        shop = shop,
        hit = hit.run {
            Recommendation.Hit(
                count = count,
                items = items.map {
                    Recommendation.Hit.Item(
                        found = it.found,
                        unitPrice = it.unitPrice,
                        requested = it.requested,
                        purchaseQuantity = it.purchaseQuantity,
                    )
                },
            )
        },
        miss = miss.run {
            Recommendation.Miss(items = items, count = count)
        },
        expenditure = expenditure.run {
            Recommendation.Expenditure(unit = unit, total = total)
        },
    )
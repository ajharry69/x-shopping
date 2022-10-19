package co.ke.xently.shopping.features.utils

import co.ke.xently.shopping.features.BuildConfig.INTERNAL_DEEPLINK_SCHEME


@Suppress("RegExpRedundantEscape")
fun String.buildRoute(vararg args: Pair<String, Any>): String {
    /*
    TODO: Consider throwing an error if args have a key that wasn't matched or if route path
      has a (required) argument that's not fulfilled i.e. path still contains path/{xx}/
     */
    var route = this
    for (arg in args) {
        route = route.replace("{${arg.first}}", arg.second.toString())
    }
    val segments = route.split("?", limit = 2)
    val path = segments[0].replace(Regex("\\{.+\\}"), "")
    var queries = ""
    if (segments.size > 1) {
        queries = segments[1].replace(Regex("=\\{.+\\}"), "").split("&").filter { it.contains('=') }
            .joinToString("&")
    }
    return path + if (queries.isNotBlank()) {
        "?$queries"
    } else {
        ""
    }
}

object Routes {
    object Dashboard {
        override fun toString(): String {
            return javaClass.name
        }
    }

    object Users {
        const val SIGN_IN = "users/signin/"
        const val SIGN_UP = "users/signup/"
        const val VERIFY_ACCOUNT = "users/verify-account/"
        const val REQUEST_PASSWORD_RESET = "users/request-password-reset/?isChange={isChange}"
        const val RESET_PASSWORD = "users/reset-password/"
        override fun toString(): String {
            return javaClass.name
        }

        object Deeplinks {
            const val SIGN_IN = "$INTERNAL_DEEPLINK_SCHEME://users/signin/"
            const val SIGN_UP = "$INTERNAL_DEEPLINK_SCHEME://users/signup/"
        }
    }

    object Products {
        const val LIST = "products/"
        const val SEARCH = "products/search/"
        const val DETAIL = "products/{id}/"
        override fun toString(): String {
            return javaClass.name
        }
    }

    object Taxes {
        const val LIST = "taxes/"
        const val SEARCH = "taxes/search/"
        const val DETAIL = "taxes/{id}/"
        override fun toString(): String {
            return javaClass.name
        }
    }

    object Customers {
        const val LIST = "customers/"
        const val SEARCH = "customers/search/"
        const val DETAIL = "customers/{id}/"
        override fun toString(): String {
            return javaClass.name
        }
    }

    object BusinessRegistration {
        const val LIST = "business-registration/"
        const val SEARCH = "business-registration/search/"
        const val DETAIL = "business-registration/basic-info/"
        const val TAX_DETAIL = "business-registration/tax-details/"
        override fun toString(): String {
            return javaClass.name
        }

        object Deeplinks {
            const val LIST = "$INTERNAL_DEEPLINK_SCHEME://business-registration/"
        }
    }

    object Purchases {
        const val LIST = "purchases/"
        const val SEARCH = "purchases/search/"
        const val DETAIL = "purchases/{id}/"
        const val PRODUCTS = "purchases/products/"
        override fun toString(): String {
            return javaClass.name
        }
    }

    object Sales {
        const val SEARCH = "sales/search/"
        const val LIST = "sales/"
        const val DETAIL = "sales/{id}/"
        const val PAYMENT = "sales/{id}/payment/"
        const val CONFIRMATION = "sales/{id}/confirmation/"
        const val PRODUCTS = "sales/products/"
        override fun toString(): String {
            return javaClass.name
        }
    }

    object Reports {
        const val LIST = "reports/"
        const val SALES = "${LIST}sales/"
        const val PURCHASE = "${LIST}purchase/"
        const val STOCK = "${LIST}stock/"
        const val SALES_RANK = "${LIST}sales-rank/"
        const val PROFIT_MARGIN = "${LIST}profit-margin/"
        override fun toString(): String {
            return javaClass.name
        }
    }

    object HsCodes {
        const val LIST = "hscodes/"
        const val SEARCH = "hscodes/search/"
        override fun toString(): String {
            return javaClass.name
        }
    }
}
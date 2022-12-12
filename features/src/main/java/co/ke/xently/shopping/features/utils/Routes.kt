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
        object Deeplinks {
            const val SIGN_IN = "$INTERNAL_DEEPLINK_SCHEME://users/signin/"
            const val SIGN_UP = "$INTERNAL_DEEPLINK_SCHEME://users/signup/"
        }
    }

    object Shop {
        const val DETAIL = "shops/{id}/?name={name}"
        override fun toString(): String {
            return javaClass.name
        }
    }

    object Product {
        const val DETAIL = "products/{id}/"
        override fun toString(): String {
            return javaClass.name
        }
    }

    object Recommendation {
        const val REQUEST =
            "shopping-list/recommendations/request/?itemId={itemId}&group={group}&groupBy={groupBy}"
        const val RECOMMEND = "shopping-list/recommendations/?numberOfItems={numberOfItems}"
        override fun toString(): String {
            return javaClass.name
        }
    }
}
package co.ke.xently.shopping.features.utils

import co.ke.xently.shopping.features.BuildConfig.INTERNAL_DEEPLINK_SCHEME

object Deeplinks {
    const val SIGN_IN = "$INTERNAL_DEEPLINK_SCHEME://users/signin/"
    const val SIGN_UP = "$INTERNAL_DEEPLINK_SCHEME://users/signup/"
}
package co.ke.xently.shopping.features

import android.content.Context

val Context.fileFriendlyAppName
    get() = getString(R.string.app_name).replace("\\s+".toRegex(), "").lowercase()


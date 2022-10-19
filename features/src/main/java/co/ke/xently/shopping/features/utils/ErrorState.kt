package co.ke.xently.shopping.features.utils

import android.content.Context

fun interface ErrorState {
    fun getMessage(context: Context): String
}
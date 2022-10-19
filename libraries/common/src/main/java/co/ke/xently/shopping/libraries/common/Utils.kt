package co.ke.xently.shopping.libraries.common

object Utils {
    val isReleaseBuild by lazy {
        BuildConfig.BUILD_TYPE.lowercase().contains(Regex("^release$"))
    }
}
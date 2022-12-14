package co.ke.xently.shopping.libraries.data.source.remote

sealed class CacheControl(private val name: String) {
    override fun toString(): String {
        return name
    }

    object NoCache : CacheControl("no-cache")

    object OnlyIfCached : CacheControl("only-if-cached")

    companion object {
        fun getOrThrow(lookup: String): CacheControl {
            return when (lookup) {
                NoCache.toString() -> {
                    NoCache
                }
                OnlyIfCached.toString() -> {
                    OnlyIfCached
                }
                else -> {
                    throw NotImplementedError()
                }
            }
        }
    }
}

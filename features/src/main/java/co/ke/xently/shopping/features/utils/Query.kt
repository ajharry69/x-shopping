package co.ke.xently.shopping.features.utils

/**
 * @param size A value of less than zero indicates return all results
 */
data class Query(
    val value: String,
    val size: Int = Int.MAX_VALUE,
    val filters: Map<String, Any> = emptyMap(),
) {
    override fun toString(): String {
        return value
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + size
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Query

        if (value != other.value) return false
        if (size != other.size) return false

        return true
    }
}
package co.ke.xently.shopping.features.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable

@Stable
data class MenuItem<T : Any>(
    @StringRes
    val label: Int,
    val onClick: (T) -> Unit,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MenuItem<*>

        if (label != other.label) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label
        result = 31 * result + onClick.hashCode()
        return result
    }
}
package co.ke.xently.shopping.libraries.data.source.utils

import java.text.SimpleDateFormat
import java.util.*

object DateFormat {
    private val KENYA: Locale = Locale("en", "KE")

    val DEFAULT_SERVER_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", KENYA)

    /**
     * Format to use when formatting date shown to users
     */
    val DEFAULT_LOCAL_DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy", KENYA)

    /**
     * Format to use when formatting time shown to users
     */
    val DEFAULT_LOCAL_TIME_FORMAT = SimpleDateFormat("h:mm a", KENYA)

    /**
     * Format to use when formatting date and time shown to users
     */
    val DEFAULT_LOCAL_DATE_TIME_FORMAT = SimpleDateFormat("dd/MM/yyyy h:mm a", KENYA)

    const val DEFAULT_SERVER_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mmZ"
}
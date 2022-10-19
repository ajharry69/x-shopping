package co.ke.xently.shopping.features.ui

import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.text.input.TextFieldValue
import co.ke.xently.shopping.libraries.data.source.local.RoomTypeConverters
import java.util.*

object Savers {
    val TEXT_FIELD_VALUE = run {
        Saver<TextFieldValue, String>(
            save = {
                it.text
            },
            restore = {
                TextFieldValue(it)
            },
        )
    }

    val DATE = run {
        val defaultDateLong = -1L
        Saver<Date?, Long>(
            save = { date ->
                date?.let(RoomTypeConverters.DateConverter::dateToLong) ?: defaultDateLong
            },
            restore = { dateLong ->
                if (dateLong == defaultDateLong) {
                    null
                } else {
                    dateLong.let(RoomTypeConverters.DateConverter::longToDate)
                }
            },
        )
    }
}

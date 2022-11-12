package co.ke.xently.shopping.features.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.*
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

@Composable
fun rememberDatePickerDialog(
    @StringRes title: Int,
    select: Date? = null,
    bounds: CalendarConstraints? = null,
    onDateSelected: (Date) -> Unit = {},
): MaterialDatePicker<Long> {
    val onDateSelectedRemembered by rememberUpdatedState(onDateSelected)
    val datePicker = remember {
        MaterialDatePicker.Builder.datePicker()
            .setSelection(
                (select?.time
                    ?: Date().time) + 24.hours.toLong(DurationUnit.MILLISECONDS)
            )
            .setCalendarConstraints(bounds)
            .setTitleText(title)
            .build()
    }

    DisposableEffect(datePicker) {
        val listener = MaterialPickerOnPositiveButtonClickListener<Long> {
            if (it != null) onDateSelectedRemembered(Date(it))
        }
        datePicker.addOnPositiveButtonClickListener(listener)
        onDispose {
            datePicker.removeOnPositiveButtonClickListener(listener)
        }
    }

    return datePicker
}
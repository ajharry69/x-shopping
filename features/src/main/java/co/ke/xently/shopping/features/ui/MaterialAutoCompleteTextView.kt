package co.ke.xently.shopping.features.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import co.ke.xently.shopping.features.R
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.MaterialAutoCompleteTextView as GoogleMaterialAutoCompleteTextView

object MaterialAutoCompleteTextView {
    @Composable
    operator fun invoke(
        modifier: Modifier,
        configure: (GoogleMaterialAutoCompleteTextView) -> Unit,
    ) {
        val updatedConfiguration by rememberUpdatedState(configure)
        AndroidView(
            modifier = modifier,
            factory = { context ->
                (TextInputLayout.inflate(context,
                    R.layout.auto_complete_text_view,
                    null) as TextInputLayout).also {
                    updatedConfiguration(it.editText as GoogleMaterialAutoCompleteTextView)
                }
            },
            update = {
                updatedConfiguration(it.editText as GoogleMaterialAutoCompleteTextView)
            },
        )
    }
}
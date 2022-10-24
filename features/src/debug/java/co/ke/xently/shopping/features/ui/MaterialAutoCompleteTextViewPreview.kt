package co.ke.xently.shopping.features.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.ke.xently.shopping.features.ui.theme.XentlyTheme

@XentlyPreview
@Composable
private fun MaterialAutoCompleteTextViewPreview() {
    XentlyTheme {
        Column {
            MaterialAutoCompleteTextView(modifier = Modifier.fillMaxWidth()) {

            }
        }
    }
}
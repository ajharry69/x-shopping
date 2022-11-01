package co.ke.xently.shopping.features.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object SupportingText {
    @Composable
    operator fun <Value> invoke(config: TextFieldConfig<Value>, helpText: String? = null) {
        if (config.hasError) {
            config.error
        } else {
            helpText
        }?.takeIf(CharSequence::isNotBlank)?.let {
            if (config.expectedNumberOfCharacters == null) {
                Text(it)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(it, modifier = Modifier.weight(1f))
                    Text("${config.currentNumberOfCharacters}/${config.expectedNumberOfCharacters}")
                }
            }
        }.let {
            if (it == null && config.expectedNumberOfCharacters != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f))
                    Text("${config.currentNumberOfCharacters}/${config.expectedNumberOfCharacters}")
                }
            }
        }
    }
}
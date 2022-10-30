package co.ke.xently.shopping.features

import androidx.annotation.StringRes
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import co.ke.xently.shopping.features.ui.Savers
import co.ke.xently.shopping.features.utils.State

data class TextFieldResource<Value>(
    val value: Value,
    val label: String,
    val error: String,
    val hasError: Boolean,
    val onValueChange: (Value) -> Unit,
) {
    companion object {
        @Composable
        fun <T : Any> compileResource(
            @StringRes labelId: Int,
            valueInputs: T?,
            state: State<*>,
            shouldResetField: Boolean,
            defaultValue: (T) -> String? = { it.toString() },
            errorMessage: (State.Error) -> String?,
        ): TextFieldResource<TextFieldValue> {
            val context = LocalContext.current

            val label = stringResource(labelId)

            var value by rememberSaveable(valueInputs, stateSaver = Savers.TEXT_FIELD_VALUE) {
                mutableStateOf(TextFieldValue(valueInputs?.let(defaultValue) ?: ""))
            }

            val isFieldRequired by remember(label) {
                derivedStateOf {
                    label.endsWith('*')
                }
            }

            var usableState by remember(state) {
                mutableStateOf(state)
            }

            val error = rememberSaveable(label, value, usableState) {
                if (isFieldRequired && value.text.isBlank()) {
                    context.getString(R.string.feature_field_required, label.trimEnd { it == '*' })
                } else {
                    (usableState as? State.Error)?.let(errorMessage) ?: ""
                }
            }

            val hasError by remember(error) {
                derivedStateOf {
                    error.isNotBlank()
                }
            }

            LaunchedEffect(shouldResetField.toString()) {
                if (shouldResetField) {
                    value = TextFieldValue(valueInputs?.let(defaultValue) ?: "")
                }
            }

            return TextFieldResource(
                value = value,
                label = label,
                error = error,
                hasError = hasError,
                onValueChange = {
                    value = it
                    usableState = State.Success(null)
                },
            )
        }
    }
}
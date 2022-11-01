package co.ke.xently.shopping.features.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import co.ke.xently.shopping.features.R
import co.ke.xently.shopping.features.utils.State

object TextInputLayout {
    data class InputFieldResource<Value>(
        val value: Value,
        val label: String,
        val error: String,
        val hasError: Boolean,
        val onValueChange: (Value) -> Unit,
    ) {
        companion object {
            @Composable
            private fun <T : Any, Value> compileResource(
                @StringRes labelId: Int,
                valueInputs: T?,
                state: State<*>,
                shouldResetField: Boolean,
                saver: Saver<Value, String>,
                defaultValue: (T) -> String?,
                errorMessage: (State.Error) -> String?,
                generateDefaultValue: (String) -> Value,
                blankLookupValue: (Value) -> String,
            ): InputFieldResource<Value> {
                val context = LocalContext.current

                val label = stringResource(labelId)

                var value by rememberSaveable(valueInputs, stateSaver = saver) {
                    mutableStateOf(generateDefaultValue(valueInputs?.let(defaultValue) ?: ""))
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
                    if (isFieldRequired && blankLookupValue(value).isBlank()) {
                        context.getString(R.string.feature_field_required,
                            label.trimEnd { it == '*' })
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
                        value = generateDefaultValue(valueInputs?.let(defaultValue) ?: "")
                    }
                }

                return InputFieldResource(
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

            @Composable
            fun <T : Any> compileResource(
                @StringRes labelId: Int,
                valueInputs: T?,
                state: State<*>,
                shouldResetField: Boolean,
                defaultValue: (T) -> String? = { it.toString() },
                errorMessage: (State.Error) -> String?,
            ) = compileResource(
                labelId = labelId,
                valueInputs = valueInputs,
                state = state,
                shouldResetField = shouldResetField,
                saver = Savers.TEXT_FIELD_VALUE,
                errorMessage = errorMessage,
                defaultValue = defaultValue,
                generateDefaultValue = {
                    TextFieldValue(it)
                },
                blankLookupValue = {
                    it.text
                },
            )

            @Composable
            fun <T : Any> compileResourceForString(
                @StringRes labelId: Int,
                valueInputs: T?,
                state: State<*>,
                shouldResetField: Boolean,
                defaultValue: (T) -> String? = { it.toString() },
                errorMessage: (State.Error) -> String?,
            ) = compileResource(
                state = state,
                labelId = labelId,
                blankLookupValue = { it },
                generateDefaultValue = { it },
                valueInputs = valueInputs,
                errorMessage = errorMessage,
                defaultValue = defaultValue,
                shouldResetField = shouldResetField,
                saver = run {
                    Saver(
                        save = {
                            it
                        },
                        restore = {
                            it
                        },
                    )
                },
            )
        }
    }

    val DefaultKeyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
        capitalization = KeyboardCapitalization.Sentences,
    )

    @Composable
    fun supportingText(
        hasError: Boolean,
        error: String?,
        helpText: String?,
        currentNumberOfCharacters: Int = 0,
        expectedNumberOfCharacters: Int? = null,
    ): Boolean {
        val isError by remember(expectedNumberOfCharacters, currentNumberOfCharacters, hasError) {
            derivedStateOf {
                hasError || (expectedNumberOfCharacters != null
                        && currentNumberOfCharacters > expectedNumberOfCharacters)
            }
        }
        if (isError) {
            error
        } else {
            helpText
        }?.takeIf(CharSequence::isNotBlank)?.let {
            if (expectedNumberOfCharacters == null) {
                Text(it)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(it, modifier = Modifier.weight(1f))
                    Text("${currentNumberOfCharacters}/${expectedNumberOfCharacters}")
                }
            }
        }.let {
            if (it == null && expectedNumberOfCharacters != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f))
                    Text("${currentNumberOfCharacters}/${expectedNumberOfCharacters}")
                }
            }
        }

        return isError
    }

    @Composable
    operator fun invoke(
        modifier: Modifier,
        value: TextFieldValue,
        enabled: Boolean = true,
        readOnly: Boolean = false,
        singleLine: Boolean = true,
        isError: Boolean = false,
        maxLines: Int = Int.MAX_VALUE,
        error: String = "",
        helpText: String? = null,
        numberOfCharacters: Int? = null,
        textStyle: TextStyle = LocalTextStyle.current,
        keyboardOptions: KeyboardOptions = DefaultKeyboardOptions,
        keyboardActions: KeyboardActions = KeyboardActions(),
        trailingIcon: @Composable (() -> Unit)? = null,
        label: String? = null,
        visualTransformation: VisualTransformation = VisualTransformation.None,
        onValueChange: (TextFieldValue) -> Unit,
    ) {
        var isError1 by remember(isError) {
            mutableStateOf(isError)
        }
        val context = LocalContext.current
        TextField(
            value = value,
            enabled = enabled,
            readOnly = readOnly,
            maxLines = maxLines,
            singleLine = singleLine,
            isError = isError1,
            textStyle = textStyle,
            onValueChange = onValueChange,
            label = label?.let { { Text(it) } },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            modifier = modifier.semantics {
                testTag = context.getString(R.string.content_description_text_field, label ?: "")
                    .trimStart()
            },
            supportingText = {
                isError1 = supportingText(
                    hasError = isError,
                    error = error,
                    helpText = helpText,
                    currentNumberOfCharacters = value.text.length,
                    expectedNumberOfCharacters = numberOfCharacters,
                )
            }
        )
    }
}
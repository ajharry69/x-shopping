package co.ke.xently.shopping.features.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import co.ke.xently.shopping.features.R
import co.ke.xently.shopping.features.utils.State

class TextFieldConfig<Value> private constructor(
    val value: Value,
    val label: String,
    val error: String,
    val hasError: Boolean,
    val currentNumberOfCharacters: Int,
    val expectedNumberOfCharacters: Int?,
    val onValueChange: (Value) -> Unit,
) {
    companion object {
        @Composable
        private operator fun <T : Any, Value> invoke(
            label: String,
            valueInputs: T?,
            state: State<*>,
            shouldResetField: Boolean,
            saver: Saver<Value, String>,
            defaultValue: (T) -> String?,
            expectedNumberOfCharacters: Int?,
            errorMessage: (State.Error) -> String?,
            generateDefaultValue: (String) -> Value,
            blankLookupValue: (Value) -> String,
            currentNumberOfCharacters: (Value) -> Int,
            extraErrorChecks: ((Value) -> Pair<Boolean, String>)?,
        ): TextFieldConfig<Value> {
            val context = LocalContext.current

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
                    context.getString(R.string.feature_field_required, label.trimEnd { it == '*' })
                } else {
                    val check = extraErrorChecks?.invoke(value) ?: (false to "")
                    if (check.first) {
                        check.second
                    } else {
                        (usableState as? State.Error)?.let(errorMessage) ?: ""
                    }
                }
            }

            val hasError by remember(error) {
                derivedStateOf {
                    error.isNotBlank()
                }
            }

            val currentNumberOfChars by remember(value) {
                derivedStateOf {
                    currentNumberOfCharacters(value)
                }
            }

            val hasErrorFinal by remember(
                hasError,
                currentNumberOfChars,
                expectedNumberOfCharacters,
            ) {
                derivedStateOf {
                    hasError || (expectedNumberOfCharacters != null &&
                            currentNumberOfChars > expectedNumberOfCharacters)
                }
            }

            LaunchedEffect(shouldResetField.toString()) {
                if (shouldResetField) {
                    value = generateDefaultValue(valueInputs?.let(defaultValue) ?: "")
                }
            }

            return TextFieldConfig(
                value = value,
                label = label,
                error = error,
                hasError = hasErrorFinal,
                onValueChange = {
                    value = it
                    usableState = State.Success(null)
                },
                currentNumberOfCharacters = currentNumberOfChars,
                expectedNumberOfCharacters = expectedNumberOfCharacters,
            )
        }

        @Composable
        operator fun <T : Any> invoke(
            label: String,
            state: State<*>,
            valueInputs: T? = null,
            shouldResetField: Boolean = false,
            expectedNumberOfCharacters: Int? = null,
            defaultValue: (T) -> String? = { it.toString() },
            extraErrorChecks: ((TextFieldValue) -> Pair<Boolean, String>)? = null,
            errorMessage: (State.Error) -> String?,
        ) = invoke(
            label = label,
            valueInputs = valueInputs,
            state = state,
            shouldResetField = shouldResetField,
            saver = Savers.TEXT_FIELD_VALUE,
            errorMessage = errorMessage,
            defaultValue = defaultValue,
            generateDefaultValue = {
                TextFieldValue(it, selection = TextRange(it.length))
            },
            blankLookupValue = {
                it.text
            },
            expectedNumberOfCharacters = expectedNumberOfCharacters,
            currentNumberOfCharacters = { it.text.length },
            extraErrorChecks = extraErrorChecks,
        )

        @Composable
        operator fun <T : Any> invoke(
            @StringRes labelId: Int,
            state: State<*> = State.Success(null),
            valueInputs: T? = null,
            shouldResetField: Boolean = false,
            expectedNumberOfCharacters: Int? = null,
            defaultValue: (T) -> String? = { it.toString() },
            extraErrorChecks: ((TextFieldValue) -> Pair<Boolean, String>)? = null,
            errorMessage: (State.Error) -> String? = {null},
        ) = invoke(
            label = stringResource(labelId),
            valueInputs = valueInputs,
            state = state,
            defaultValue = defaultValue,
            errorMessage = errorMessage,
            shouldResetField = shouldResetField,
            expectedNumberOfCharacters = expectedNumberOfCharacters,
            extraErrorChecks = extraErrorChecks,
        )

        @Composable
        fun <T : Any> forString(
            label: String,
            state: State<*>,
            valueInputs: T? = null,
            shouldResetField: Boolean = false,
            expectedNumberOfCharacters: Int? = null,
            defaultValue: (T) -> String? = { it.toString() },
            extraErrorChecks: ((String) -> Pair<Boolean, String>)? = null,
            errorMessage: (State.Error) -> String?,
        ) = invoke(
            state = state,
            label = label,
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
            expectedNumberOfCharacters = expectedNumberOfCharacters,
            currentNumberOfCharacters = { it.length },
            extraErrorChecks = extraErrorChecks,
        )

        @Composable
        fun <T : Any> forString(
            @StringRes labelId: Int,
            state: State<*>,
            valueInputs: T? = null,
            shouldResetField: Boolean = false,
            expectedNumberOfCharacters: Int? = null,
            defaultValue: (T) -> String? = { it.toString() },
            extraErrorChecks: ((String) -> Pair<Boolean, String>)? = null,
            errorMessage: (State.Error) -> String?,
        ) = forString(
            state = state,
            valueInputs = valueInputs,
            defaultValue = defaultValue,
            errorMessage = errorMessage,
            label = stringResource(labelId),
            shouldResetField = shouldResetField,
            extraErrorChecks = extraErrorChecks,
            expectedNumberOfCharacters = expectedNumberOfCharacters,
        )
    }
}
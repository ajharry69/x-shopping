package co.ke.xently.shopping.features.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
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
import androidx.compose.ui.window.PopupProperties
import co.ke.xently.shopping.features.R

@Composable
fun stringRes(@StringRes string: Int, @StringRes vararg args: Int): String {
    return stringResource(string, *args.map {
        stringResource(it)
    }.toTypedArray())
}

val DefaultKeyboardOptions = KeyboardOptions.Default.copy(
    imeAction = ImeAction.Next,
    capitalization = KeyboardCapitalization.Sentences,
)

@Composable
fun TextInputLayout(
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
    onValueChange: (TextFieldValue) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    label: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        val flagAsError by remember(numberOfCharacters, value.text, isError) {
            derivedStateOf {
                isError || (numberOfCharacters != null && value.text.length > numberOfCharacters)
            }
        }
        TextField(
            value = value,
            enabled = enabled,
            readOnly = readOnly,
            maxLines = maxLines,
            singleLine = singleLine,
            isError = flagAsError,
            textStyle = textStyle,
            onValueChange = onValueChange,
            label = label?.let { { Text(it) } },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    testTag = context
                        .getString(
                            R.string.content_description_text_field,
                            label ?: "",
                        )
                        .trimStart()
                },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = VIEW_SPACE, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val counterColor = if (flagAsError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = .6f)
            }
            if (isError) {
                Text(
                    text = error,
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            testTag = context.getString(
                                R.string.test_tag_text_field_error_text,
                                label ?: "",
                            )
                        },
                    color = counterColor,
                    style = MaterialTheme.typography.labelSmall, // Changed from `caption`
                )
            } else if (!helpText.isNullOrBlank()) {
                Text(
                    text = helpText,
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            testTag = context.getString(
                                R.string.test_tag_text_field_help_text,
                                label ?: "",
                            )
                        },
                    color = counterColor,
                    style = MaterialTheme.typography.labelSmall, // Changed from `caption`
                )
            } else {
                Box(modifier = Modifier.weight(1f))
            }
            if (numberOfCharacters != null) {
                Text(
                    text = "${value.text.length}/${numberOfCharacters}",
                    color = counterColor,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

/**
 * TODO: Replace with - https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#ExposedDropdownMenuBox(kotlin.Boolean,kotlin.Function1,androidx.compose.ui.Modifier,kotlin.Function1)
 */
@Composable
fun <T> AutoCompleteTextField(
    value: TextFieldValue,
    suggestions: List<T>,
    onValueChange: (TextFieldValue) -> Unit,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    error: String = "",
    helpText: String? = null,
    keyboardOptions: KeyboardOptions = DefaultKeyboardOptions,
    keyboardActions: KeyboardActions = KeyboardActions(),
    label: String? = null,
    wasSuggestionPicked: (Boolean) -> Unit = {},
    trailingIcon: @Composable (() -> Unit)? = null,
    suggestionItemContent: @Composable ((T) -> Unit),
) {
    val context = LocalContext.current
    var showDropdownMenu by remember { mutableStateOf(false) }
    var wasSuggestionSelected by remember { mutableStateOf(false) }

    SideEffect {
        wasSuggestionPicked.invoke(wasSuggestionSelected)
    }

    Box(modifier = modifier) {
        TextInputLayout(
            value = value,
            label = label,
            error = error,
            enabled = enabled,
            readOnly = readOnly,
            isError = isError,
            helpText = helpText,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusEvent {
                    if (!it.isFocused) {
                        showDropdownMenu = false
                    }
                },
            onValueChange = {
                wasSuggestionSelected = false
                onValueChange(it)
                showDropdownMenu = it.text.isNotBlank()
            },
            trailingIcon = trailingIcon ?: {
                IconButton(onClick = { showDropdownMenu = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
        )
        DropdownMenu(
            expanded = showDropdownMenu && suggestions.isNotEmpty(),
            onDismissRequest = {
                showDropdownMenu = false
            },
            modifier = Modifier
                .wrapContentWidth()
                .semantics {
                    testTag = context.getString(
                        R.string.test_tag_dropdown_menu_suggestions,
                        label ?: "",
                    )
                },
            properties = PopupProperties(focusable = false),
        ) {
            suggestions.forEach {
                DropdownMenuItem(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = {
                        suggestionItemContent(it)
                    },
                    onClick = {
                        onOptionSelected(it)
                        showDropdownMenu = false
                        wasSuggestionSelected = true
                    },
                )
            }
        }
    }
}
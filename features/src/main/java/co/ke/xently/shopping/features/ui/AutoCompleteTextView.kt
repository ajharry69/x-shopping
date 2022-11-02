package co.ke.xently.shopping.features.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue

object AutoCompleteTextView {
    @Composable
    private operator fun <T> invoke(
        modifier: Modifier,
        suggestions: Iterable<T>,
        onSuggestionSelected: (T) -> Unit,
        suggestionText: (@Composable (T) -> Unit)? = null,
        textField: @Composable ExposedDropdownMenuBoxScope.(expanded: Boolean) -> Unit,
    ) {
        var expanded by rememberSaveable {
            mutableStateOf(false)
        }
        ExposedDropdownMenuBox(
            modifier = modifier,
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            textField(expanded)

            val areSuggestionsAvailable by remember(suggestions) {
                derivedStateOf {
                    suggestions.firstOrNull() != null
                }
            }
            if (areSuggestionsAvailable) {
                ExposedDropdownMenu(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    suggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            modifier = Modifier.fillMaxWidth(),
                            text = {
                                if (suggestionText == null) {
                                    Text(suggestion.toString())
                                } else {
                                    suggestionText(suggestion)
                                }
                            },
                            onClick = {
                                onSuggestionSelected(suggestion)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
        }
    }

    @Composable
    operator fun <T> invoke(
        modifier: Modifier,
        suggestions: Iterable<T>,
        config: TextFieldConfig<TextFieldValue>,
        helpText: String? = null,
        onSuggestionSelected: ((T) -> Unit)? = null,
        onQueryChange: (String) -> Unit,
        keyboardOptions: KeyboardOptions? = null,
        keyboardActions: KeyboardActions? = null,
        emptySuggestionsTrailingIcon: @Composable (() -> Unit)? = null,
        suggestionText: @Composable ((T) -> Unit)? = null,
    ) {
        AutoCompleteTextView(
            modifier = modifier,
            suggestions = suggestions,
            suggestionText = suggestionText,
            onSuggestionSelected = {
                if (onSuggestionSelected == null) {
                    val text = it.toString()
                    config.onValueChange(TextFieldValue(text, selection = TextRange(text.length)))
                } else {
                    onSuggestionSelected(it)
                }
            },
        ) { expanded ->
            TextField(
                singleLine = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                value = config.value,
                isError = config.hasError,
                label = {
                    Text(config.label)
                },
                onValueChange = {
                    config.onValueChange(it)
                    onQueryChange(it.text)
                },
                supportingText = {
                    SupportingText(config = config, helpText = helpText)
                },
                trailingIcon = {
                    val showAlternativeTrailingIcon by remember(suggestions, config.value) {
                        derivedStateOf {
                            suggestions.firstOrNull() == null
                                    && config.value.text.isNotBlank()
                                    && emptySuggestionsTrailingIcon != null
                        }
                    }
                    AnimatedContent(targetState = showAlternativeTrailingIcon) {
                        if (showAlternativeTrailingIcon) {
                            emptySuggestionsTrailingIcon!!()
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    }
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                keyboardActions = keyboardActions ?: KeyboardActions.Default,
                keyboardOptions = keyboardOptions
                    ?: DefaultKeyboardOptions.copy(imeAction = ImeAction.Search),
            )
        }
    }
}
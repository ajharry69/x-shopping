package co.ke.xently.shopping.features.ui

import androidx.compose.foundation.layout.fillMaxWidth
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
        resource: TextFieldConfig<TextFieldValue>,
        helpText: String? = null,
        onSuggestionSelected: ((T) -> Unit)? = null,
        onMeasurementUnitQueryChange: (String) -> Unit,
        suggestionText: @Composable ((T) -> Unit)? = null,
    ) {
        AutoCompleteTextView(
            modifier = modifier,
            suggestions = suggestions,
            suggestionText = suggestionText,
            onSuggestionSelected = {
                if (onSuggestionSelected == null) {
                    val text = it.toString()
                    resource.onValueChange(TextFieldValue(text, selection = TextRange(text.length)))
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
                value = resource.value,
                isError = resource.hasError,
                label = { Text(resource.label) },
                onValueChange = {
                    resource.onValueChange(it)
                    onMeasurementUnitQueryChange(it.text)
                },
                keyboardOptions = DefaultKeyboardOptions.copy(imeAction = ImeAction.Search),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                supportingText = {
                    SupportingText(config = resource, helpText = helpText)
                },
            )
        }
    }
}
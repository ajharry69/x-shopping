package co.ke.xently.shopping.features.products.shared.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import co.ke.xently.shopping.features.products.shared.R
import co.ke.xently.shopping.features.ui.AutoCompleteTextView
import co.ke.xently.shopping.features.ui.TextFieldConfig
import co.ke.xently.shopping.features.ui.fillMaxWidthHorizontalPadding
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.AbstractAttribute
import kotlinx.coroutines.launch

data class AttributesInput(
    val nameConfig: TextFieldConfig<TextFieldValue>,
    val valueConfig: TextFieldConfig<TextFieldValue>,
    val attributes: List<AbstractAttribute>,
) {
    companion object {
        @Composable
        operator fun <T : AbstractAttribute> invoke(
            snackbarState: SnackbarHostState,
            state: State<*>,
            shouldResetFields: Boolean,
            default: List<AbstractAttribute>?,
            suggestions: List<AbstractAttribute>,
            nameSuggestions: List<String>,
            onAttributeNameQueryChange: (String) -> Unit,
            onAttributeValueQueryChange: (String, String) -> Unit,
            errorMessage: (State.Error) -> String?,
            create: (AbstractAttribute) -> T,
        ): AttributesInput {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            var attributeBeingEdited by remember {
                mutableStateOf<Pair<Int, AbstractAttribute>?>(null)
            }
            val nameConfig = TextFieldConfig(
                labelId = R.string.feature_products_shared_detail_input_field_label_attribute_name,
                state = state,
                valueInputs = attributeBeingEdited?.second?.name,
                shouldResetField = shouldResetFields,
                errorMessage = errorMessage,
            )
            val valueConfig = TextFieldConfig(
                labelId = R.string.feature_products_shared_detail_input_field_label_attribute_value,
                state = state,
                valueInputs = attributeBeingEdited?.second?.value,
                shouldResetField = shouldResetFields,
                errorMessage = errorMessage,
            )

            val selectedAttributes = remember(default, shouldResetFields) {
                mutableStateListOf(*(default ?: emptyList()).toTypedArray())
            }

            val onSuggestionSelected: (AbstractAttribute) -> Unit = { attribute ->
                attributeBeingEdited.also {
                    if (it == null) {
                        selectedAttributes.add(attribute)
                    } else {
                        selectedAttributes[it.first] = attribute
                    }
                }
                nameConfig.onValueChange(TextFieldValue())
                valueConfig.onValueChange(TextFieldValue())
                attributeBeingEdited = null
            }

            val onImeActionClicked: () -> Unit = {
                val attributeName = nameConfig.value.text.trim().takeIf(String::isNotBlank)

                val attributeValue = valueConfig.value.text.trim().takeIf(String::isNotBlank)

                if (attributeName == null) {
                    scope.launch {
                        snackbarState.showSnackbar(
                            context.getString(R.string.feature_products_shared_error_requires_attr_name),
                            withDismissAction = true,
                        )
                    }
                } else if (attributeValue == null) {
                    scope.launch {
                        snackbarState.showSnackbar(
                            context.getString(R.string.feature_products_shared_error_requires_attr_value),
                            withDismissAction = true,
                        )
                    }
                } else {
                    val attribute = object : AbstractAttribute() {
                        override val name: String
                            get() = attributeName
                        override val value: String
                            get() = attributeValue
                    }
                    onSuggestionSelected(create(attribute))
                }
            }

            Column(
                modifier = Modifier.fillMaxWidthHorizontalPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AutoCompleteTextView(
                        modifier = Modifier.weight(1f),
                        suggestions = nameSuggestions,
                        config = nameConfig,
                        onQueryChange = onAttributeNameQueryChange,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onImeActionClicked()
                        }),
                        emptySuggestionsTrailingIcon = {
                            IconButton(onClick = onImeActionClicked) {
                                Icon(Icons.Default.Done, contentDescription = null)
                            }
                        },
                    )
                    AutoCompleteTextView(
                        modifier = Modifier.weight(1f),
                        suggestions = suggestions,
                        config = valueConfig,
                        onQueryChange = {
                            onAttributeValueQueryChange(nameConfig.value.text, it)
                        },
                        onSuggestionSelected = onSuggestionSelected,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onImeActionClicked()
                        }),
                        emptySuggestionsTrailingIcon = {
                            IconButton(onClick = onImeActionClicked) {
                                Icon(Icons.Default.Done, contentDescription = null)
                            }
                        },
                    )
                }

                val showSelectedAttributes by remember(selectedAttributes) {
                    derivedStateOf {
                        !selectedAttributes.isEmpty()
                    }
                }

                if (showSelectedAttributes) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        selectedAttributes.forEachIndexed { idx, it ->
                            ElevatedAssistChip(
                                onClick = { },
                                label = {
                                    Text(it.toString())
                                },
                                trailingIcon = {
                                    IconButton(onClick = { attributeBeingEdited = idx to it }) {
                                        Icon(
                                            Icons.Default.ModeEdit,
                                            contentDescription = null,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }
            return AttributesInput(nameConfig, valueConfig, selectedAttributes)
        }
    }
}
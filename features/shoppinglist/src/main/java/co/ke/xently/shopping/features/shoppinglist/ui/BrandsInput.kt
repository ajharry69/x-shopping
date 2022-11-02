package co.ke.xently.shopping.features.shoppinglist.ui

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
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.ui.AutoCompleteTextView
import co.ke.xently.shopping.features.ui.TextFieldConfig
import co.ke.xently.shopping.features.ui.fillMaxWidthHorizontalPadding
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.AbstractBrand

data class BrandsInput(
    val config: TextFieldConfig<TextFieldValue>,
    val brands: List<AbstractBrand>,
) {
    companion object {
        @Composable
        operator fun invoke(
            state: State<*>,
            shouldResetFields: Boolean,
            defaultBrands: List<AbstractBrand>?,
            suggestions: List<AbstractBrand>,
            onQueryChange: (String) -> Unit,
            errorMessage: (State.Error) -> String?,
            createBrandFromName: (String) -> AbstractBrand,
        ): BrandsInput {
            var brandBeingEdited by remember {
                mutableStateOf<Pair<Int, AbstractBrand>?>(null)
            }
            val brandConfig = TextFieldConfig(
                labelId = R.string.feature_shoppinglist_detail_input_field_label_brand,
                state = state,
                valueInputs = brandBeingEdited?.second?.name,
                shouldResetField = shouldResetFields,
                errorMessage = errorMessage,
            )

            val selectedBrands = remember(defaultBrands, shouldResetFields) {
                mutableStateListOf(*(defaultBrands ?: emptyList()).toTypedArray())
            }

            val onSuggestionSelected: (AbstractBrand) -> Unit = { brand ->
                brandBeingEdited.also {
                    if (it == null) {
                        selectedBrands.add(brand)
                    } else {
                        selectedBrands[it.first] = brand
                    }
                }
                brandConfig.onValueChange(TextFieldValue())
                brandBeingEdited = null
            }

            Column(
                modifier = Modifier.fillMaxWidthHorizontalPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AutoCompleteTextView(
                    modifier = Modifier.fillMaxWidth(),
                    suggestions = suggestions,
                    config = brandConfig,
                    onQueryChange = onQueryChange,
                    onSuggestionSelected = onSuggestionSelected,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        val brandName = brandConfig.value.text.trim().takeIf(String::isNotBlank)
                            ?: return@KeyboardActions
                        onSuggestionSelected(createBrandFromName(brandName))
                    }),
                    emptySuggestionsTrailingIcon = {
                        IconButton(onClick = {
                            onSuggestionSelected(createBrandFromName(brandConfig.value.text.trim()))
                        }) {
                            Icon(Icons.Default.Done, contentDescription = null)
                        }
                    },
                )

                val showSelectedBrands by remember(selectedBrands) {
                    derivedStateOf {
                        !selectedBrands.isEmpty()
                    }
                }

                if (showSelectedBrands) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        selectedBrands.forEachIndexed { idx, it ->
                            ElevatedAssistChip(
                                onClick = { },
                                label = {
                                    Text(it.name)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { brandBeingEdited = idx to it }) {
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
            return BrandsInput(brandConfig, selectedBrands)
        }
    }
}
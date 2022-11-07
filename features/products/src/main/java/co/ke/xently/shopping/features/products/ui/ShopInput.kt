package co.ke.xently.shopping.features.products.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import co.ke.xently.shopping.features.products.R
import co.ke.xently.shopping.features.ui.AutoCompleteTextView
import co.ke.xently.shopping.features.ui.TextFieldConfig
import co.ke.xently.shopping.features.ui.fillMaxWidthHorizontalPadding
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.Shop

data class ShopInput(val config: TextFieldConfig<TextFieldValue>, val shop: Shop) {
    companion object {
        @Composable
        operator fun invoke(
            modifier: Modifier,
            state: State<*>,
            shouldResetFields: Boolean,
            default: Shop?,
            suggestions: List<Shop>,
            onQueryChange: (String) -> Unit,
            errorMessage: (State.Error) -> String?,
            onAddShopClicked: (TextFieldConfig<TextFieldValue>) -> Unit,
        ): ShopInput {
            val config: TextFieldConfig<TextFieldValue> = TextFieldConfig(
                labelId = R.string.feature_products_detail_input_field_label_shop,
                state = state,
                valueInputs = default?.name,
                shouldResetField = shouldResetFields,
                errorMessage = errorMessage,
            )

            var selectedShop by remember(default, shouldResetFields) {
                mutableStateOf(default ?: Shop.DEFAULT_INSTANCE)
            }

            AutoCompleteTextView(
                modifier = modifier,
                suggestions = suggestions,
                config = config,
                onQueryChange = onQueryChange,
                onSuggestionSelected = { selectedShop = it },
                emptySuggestionsTrailingIcon = {
                    IconButton(onClick = { onAddShopClicked(config) }) {
                        Icon(Icons.Default.AddBusiness, contentDescription = null)
                    }
                },
            )
            return ShopInput(config, selectedShop)
        }
    }
}
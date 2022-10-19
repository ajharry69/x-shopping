package co.ke.xently.shopping.features.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import co.ke.xently.shopping.features.R

object ConfirmableDelete {
    @Composable
    operator fun <T> invoke(
        itemName: (T?) -> String? = {
            it?.toString()
        },
        onDelete: (T) -> Unit,
    ): (T) -> Unit {
        var itemToDelete: T? by remember {
            mutableStateOf(null)
        }
        if (itemToDelete != null) {
            val currentItemName by rememberUpdatedState(itemName)
            val onNegativeButtonClick = { itemToDelete = null }

            AlertDialog(
                title = {
                    Text(text = stringResource(R.string.confirm_deletion_dialog_title))
                },
                text = {
                    Text(
                        text = stringResource(
                            R.string.confirm_deletion_dialog_message,
                            currentItemName(itemToDelete) ?: "",
                        ),
                    )
                },
                onDismissRequest = onNegativeButtonClick,
                dismissButton = {
                    TextButton(onClick = onNegativeButtonClick) {
                        Text(text = stringResource(android.R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDelete(itemToDelete!!)
                            itemToDelete = null
                        },
                    ) {
                        Text(text = stringResource(R.string.delete))
                    }
                },
            )
        }
        return { itemToDelete = it }
    }
}
package co.ke.xently.shopping.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import co.ke.xently.shopping.R

internal object SignoutOpsDialog {
    @Composable
    operator fun invoke(onDismissRequest: () -> Unit, onConfirmClick: (Boolean) -> Unit) {
        var removeBusinesses by remember {
            mutableStateOf(false)
        }
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(text = stringResource(R.string.confirm_signout_dialog_title))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmClick(removeBusinesses)
                        onDismissRequest()
                    },
                ) {
                    Text(text = stringResource(R.string.dashboard_item_logout))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
            text = {
                val onCheckedChanged: (Boolean) -> Unit = { selected ->
                    removeBusinesses = selected
                }
                Row(
                    modifier = Modifier
                        .toggleable(
                            value = removeBusinesses,
                            role = Role.Checkbox,
                            onValueChange = onCheckedChanged,
                        )
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = removeBusinesses,
                        onCheckedChange = onCheckedChanged,
                    )
                    val textColor = if (removeBusinesses) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                    Column {
                        Text(
                            color = textColor,
                            text = stringResource(R.string.table_name_display_businesses),
                        )
                        Text(
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = textColor.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall,
                            text = stringResource(R.string.clear_businesses_side_effect),
                        )
                    }
                }
            },
        )
    }
}
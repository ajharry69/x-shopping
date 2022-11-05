package co.ke.xently.shopping.features.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import co.ke.xently.shopping.features.R

object TopAppBarWithProgressIndicator {
    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        showProgressIndicator: Boolean = false,
        topAppBar: @Composable () -> Unit,
    ) {
        val context = LocalContext.current
        Column(modifier = modifier) {
            topAppBar()
            if (showProgressIndicator) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription =
                                context.getString(R.string.content_description_progress_bar)
                        },
                )
            }
        }
    }
}
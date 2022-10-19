package co.ke.xently.shopping.features.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import co.ke.xently.shopping.features.R

@Composable
fun ToolbarWithProgressbar(
    title: String,
    onNavigationIconClicked: () -> Unit,
    showProgress: Boolean = false,
    colors: TopAppBarColors = TopAppBarDefaults.smallTopAppBarColors(),
    navigationIcon: @Composable () -> Unit = {
        Icon(Icons.Default.ArrowBack, stringResource(R.string.content_description_move_back))
    },
    subTitle: String? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SmallTopAppBar(
            colors = colors,
            actions = actions,
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                IconButton(onNavigationIconClicked, content = navigationIcon)
            },
            title = {
                if (subTitle.isNullOrBlank()) {
                    Text(title)
                } else {
                    Column {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text(subTitle, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
        )
        if (showProgress) {
            val progressbarDescription = stringResource(R.string.content_description_progress_bar)
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = progressbarDescription },
            )
        }
    }
}
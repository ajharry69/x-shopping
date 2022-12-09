package co.ke.xently.shopping.features.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.ke.xently.shopping.features.R
import co.ke.xently.shopping.libraries.data.source.remote.ExceptionUtils.getErrorMessage
import co.ke.xently.shopping.libraries.data.source.utils.RetryError

@Composable
fun FullscreenError(
    modifier: Modifier,
    error: Throwable,
    retryError: RetryError = RetryError(),
    click: ErrorButtonClick = ErrorButtonClick(),
    preErrorContent: @Composable ColumnScope.(Throwable) -> Unit = {},
    postErrorContent: @Composable ColumnScope.(Throwable) -> Unit = {
        ErrorButton(it, retryError = retryError, click = click)
    },
) {
    val context = LocalContext.current
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            preErrorContent(error)
            Text(
                textAlign = TextAlign.Center,
                text = error.getErrorMessage(context = context),
            )
            postErrorContent(error)
        }
    }
}

const val PLACEHOLDER_COUNT_SMALL_ITEM_SIZE = 30
const val PLACEHOLDER_COUNT_MEDIUM_ITEM_SIZE = 10

const val TEST_TAG_CIRCULAR_PROGRESS_BAR = "TEST_TAG_CIRCULAR_PROGRESS_BAR"

@Composable
fun <T> FullscreenLoading(
    modifier: Modifier,
    placeholder: (() -> T)? = null,
    numberOfPlaceholders: Int = PLACEHOLDER_COUNT_MEDIUM_ITEM_SIZE,
    placeholderContent: @Composable (LazyItemScope.(item: T) -> Unit) = {},
) {
    if (placeholder == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.semantics {
                    testTag = TEST_TAG_CIRCULAR_PROGRESS_BAR
                },
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(
                itemContent = placeholderContent,
                items = List(numberOfPlaceholders) {
                    placeholder.invoke()
                },
            )
        }
    }
}

val EMPTY_LIST_PREFIX = mutableMapOf<String, String>()

@Composable
inline fun <reified T : Any> FullscreenEmptyList(
    modifier: Modifier,
    error: String? = null,
    preContent: @Composable ColumnScope.() -> Unit = {},
    postContent: @Composable ColumnScope.() -> Unit = {},
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            preContent()
            Text(
                textAlign = TextAlign.Center,
                text = error ?: stringResource(
                    R.string.error_message_generic_empty_list,
                    EMPTY_LIST_PREFIX.getOrPut(T::class.java.simpleName) {
                        T::class.java.simpleName.mapIndexed { i, c -> if (i != 0 && c.isUpperCase()) " $c" else "$c" }
                            .joinToString("") { it }.lowercase()
                    },
                )
            )
            postContent()
        }
    }
}
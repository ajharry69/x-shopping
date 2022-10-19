package co.ke.xently.shopping.features.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import co.ke.xently.shopping.features.R
import co.ke.xently.shopping.libraries.data.source.utils.RetryError
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

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
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(VIEW_SPACE),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            preErrorContent(error)
            Text(
                textAlign = TextAlign.Center,
                text = error.localizedMessage ?: stringResource(R.string.error_message_generic),
            )
            postErrorContent(error)
        }
    }
}

const val PLACEHOLDER_COUNT_SMALL_ITEM_SIZE = 30
const val PLACEHOLDER_COUNT_MEDIUM_ITEM_SIZE = 10
const val PLACEHOLDER_COUNT_LARGE_ITEM_SIZE = 5

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

@Composable
inline fun <reified T : Any> FullscreenEmptyList(
    modifier: Modifier,
    error: String? = null,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            modifier = Modifier.padding(VIEW_SPACE),
            textAlign = TextAlign.Center,
            text = error ?: stringResource(
                R.string.error_message_generic_empty_list,
                T::class.java.simpleName.mapIndexed { i, c -> if (i != 0 && c.isUpperCase()) " $c" else "$c" }
                    .joinToString("") { it }.lowercase()
            ),
        )
    }
}

@Composable
inline fun <reified T : Any> FullscreenEmptyList(modifier: Modifier, @StringRes error: Int) {
    FullscreenEmptyList<T>(modifier = modifier, error = stringResource(error))
}

@Suppress("unused")
@Composable
inline fun <reified T : Any> PagedDataScreen(
    modifier: Modifier,
    listState: LazyListState,
    items: LazyPagingItems<T>,
    snackbarHostState: SnackbarHostState,
    noinline placeholder: (() -> T)?,
    emptyListMessage: String? = null,
    retryError: RetryError = RetryError(),
    click: ErrorButtonClick = ErrorButtonClick(retryAble = { items.retry() }),
    numberOfPlaceholders: Int = PLACEHOLDER_COUNT_SMALL_ITEM_SIZE,
    noinline preErrorContent: @Composable (ColumnScope.(Throwable) -> Unit) = {},
    noinline postErrorContent: @Composable (ColumnScope.(Throwable) -> Unit) = {
        ErrorButton(it, retryError = retryError, click = click)
    },
    noinline itemContent: @Composable() (LazyItemScope.(T) -> Unit),
) {
    when (val refresh = items.loadState.refresh) {
        is LoadState.Loading -> {
            FullscreenLoading(
                modifier = modifier,
                placeholder = placeholder,
                placeholderContent = itemContent,
                numberOfPlaceholders = numberOfPlaceholders,
            )
        }
        is LoadState.Error -> {
            FullscreenError(
                modifier = modifier,
                error = refresh.error,
                preErrorContent = preErrorContent,
                postErrorContent = postErrorContent,
            )
        }
        is LoadState.NotLoading -> {
            if (items.itemCount == 0) {
                FullscreenEmptyList<T>(modifier, emptyListMessage)
            } else {
                val refreshState = rememberSwipeRefreshState(
                    isRefreshing = items.loadState.mediator?.refresh == LoadState.Loading,
                )
                SwipeRefresh(
                    modifier = modifier,
                    state = refreshState,
                    onRefresh = items::refresh,
                ) {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        items(items) { item ->
                            if (item == null) {
                                if (placeholder != null) {
                                    itemContent(placeholder.invoke())
                                }
                            } else {
                                itemContent(item)
                            }
                        }
                        item {
                            when (val loadState = items.loadState.append) {
                                is LoadState.Loading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentWidth(Alignment.CenterHorizontally)
                                    )
                                }
                                is LoadState.Error -> {
                                    val message = (loadState.error.localizedMessage
                                        ?: stringResource(R.string.error_message_generic))
                                    LaunchedEffect(message) {
                                        snackbarHostState.showSnackbar(message)
                                    }
                                }
                                is LoadState.NotLoading -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}
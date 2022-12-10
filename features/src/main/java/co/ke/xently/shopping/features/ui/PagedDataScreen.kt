package co.ke.xently.shopping.features.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import co.ke.xently.shopping.libraries.data.source.remote.ExceptionUtils.getErrorMessage
import co.ke.xently.shopping.libraries.data.source.utils.RetryError
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

object PagedDataScreen {
    @Composable
    inline operator fun <reified T : Any> invoke(
        modifier: Modifier,
        listState: LazyListState,
        items: LazyPagingItems<T>,
        snackbarHostState: SnackbarHostState,
        noinline placeholder: (() -> T)?,
        emptyListMessage: String? = null,
        retryError: RetryError = RetryError(),
        noinline onErrorClick: ((Throwable?) -> Unit)? = null,
        numberOfPlaceholders: Int = PLACEHOLDER_COUNT_SMALL_ITEM_SIZE,
        noinline key: ((item: T) -> Any)? = null,
        noinline preMessageContent: @Composable (ColumnScope.(Throwable?) -> Unit) = {},
        noinline postMessageContent: (@Composable (ColumnScope.(Throwable?) -> Unit))? = null,
        noinline itemContent: @Composable (LazyItemScope.(T) -> Unit),
    ) {
        val context = LocalContext.current
        AnimatedContent(targetState = items.loadState.refresh) { refresh ->
            when (refresh) {
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
                        retryError = retryError,
                        preMessageContent = preMessageContent,
                        postMessageContent = postMessageContent,
                        onErrorClick = onErrorClick ?: { items.retry() },
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
                                items(items, key = key) { item ->
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
                                            CircularProgressIndicator(modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentWidth(Alignment.CenterHorizontally))
                                        }
                                        is LoadState.Error -> {
                                            val message = loadState.error.getErrorMessage(context)
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
    }
}
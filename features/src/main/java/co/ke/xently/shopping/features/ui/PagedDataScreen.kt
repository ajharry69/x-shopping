package co.ke.xently.shopping.features.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import co.ke.xently.shopping.libraries.data.source.remote.ExceptionUtils.getErrorMessage
import co.ke.xently.shopping.libraries.data.source.utils.RetryError
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

object PagedDataScreen {
    @Composable
    operator fun invoke(
        state: LoadState,
        loadingContent: @Composable () -> Unit,
        errorContent: @Composable (LoadState.Error) -> Unit,
        successContent: @Composable (LoadState.NotLoading) -> Unit,
    ) {
        AnimatedContent(targetState = state) { loadState ->
            when (loadState) {
                is LoadState.Loading -> {
                    loadingContent()
                }
                is LoadState.Error -> {
                    errorContent(loadState)
                }
                is LoadState.NotLoading -> {
                    successContent(loadState)
                }
                else -> {
                    throw NotImplementedError()
                }
            }
        }
    }

    @Composable
    fun <T : Any> LazyPagingItems<T>.rememberSwipeRefreshState(): SwipeRefreshState {
        val loadState by remember(this) {
            derivedStateOf {
                loadState.mediator ?: loadState.source
            }
        }
        val isRefreshing by remember(loadState) {
            derivedStateOf {
                loadState.refresh == LoadState.Loading
            }
        }
        return rememberSwipeRefreshState(isRefreshing = isRefreshing)
    }

    @Composable
    fun <T : Any> SwipeRefreshContent(
        modifier: Modifier,
        items: LazyPagingItems<T>,
        content: @Composable () -> Unit,
    ) {
        SwipeRefresh(
            content = content,
            modifier = modifier,
            onRefresh = items::refresh,
            state = items.rememberSwipeRefreshState(),
        )
    }

    @Composable
    fun <T : Any> SwipeRefreshContent(
        modifier: Modifier,
        items: LazyPagingItems<T>,
        listState: LazyListState,
        prependContent: LazyListScope.() -> Unit,
        appendLoadingContent: @Composable () -> Unit,
        appendErrorContent: @Composable (LoadState.Error) -> Unit,
        appendSuccessContent: @Composable (LoadState.NotLoading) -> Unit,
    ) {
        SwipeRefreshContent(modifier = modifier, items = items) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                prependContent()
                item {
                    invoke(
                        state = items.loadState.append,
                        loadingContent = appendLoadingContent,
                        errorContent = appendErrorContent,
                        successContent = appendSuccessContent,
                    )
                }
            }
        }
    }

    @Composable
    fun <T : Any> SwipeRefreshContent(
        modifier: Modifier,
        items: LazyPagingItems<T>,
        listState: LazyListState,
        snackbarHostState: SnackbarHostState,
        prependContent: LazyListScope.() -> Unit,
    ) {
        val context = LocalContext.current
        SwipeRefreshContent(
            modifier = modifier,
            items = items,
            listState = listState,
            prependContent = prependContent,
            appendLoadingContent = {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally),
                )
            },
            appendErrorContent = {
                val message = it.error.getErrorMessage(context)
                LaunchedEffect(message) {
                    snackbarHostState.showSnackbar(message)
                }
            },
            appendSuccessContent = {

            },
        )
    }

    @Composable
    fun <T : Any> SwipeRefreshContent(
        modifier: Modifier,
        items: LazyPagingItems<T>,
        listState: LazyListState,
        snackbarHostState: SnackbarHostState,
        key: ((item: T) -> Any)?,
        placeholder: (() -> T)?,
        itemContent: @Composable (LazyItemScope.(T) -> Unit),
    ) {
        SwipeRefreshContent(
            items = items,
            modifier = modifier,
            listState = listState,
            snackbarHostState = snackbarHostState,
        ) {
            items(items, key = key) { item ->
                if (item == null) {
                    if (placeholder != null) {
                        itemContent(placeholder())
                    }
                } else {
                    itemContent(item)
                }
            }
        }
    }

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
        numberOfPlaceholders: Int = 30,
        noinline key: ((item: T) -> Any)? = null,
        noinline preMessageContent: @Composable (ColumnScope.(Throwable?) -> Unit) = {},
        noinline postMessageContent: (@Composable (ColumnScope.(Throwable?) -> Unit))? = null,
        noinline itemContent: @Composable (LazyItemScope.(T) -> Unit),
    ) {
        invoke(
            state = items.loadState.refresh,
            errorContent = {
                Fullscreen.Error(
                    modifier = modifier,
                    error = it.error,
                    retryError = retryError,
                    preMessageContent = preMessageContent,
                    postMessageContent = postMessageContent,
                    onErrorClick = onErrorClick ?: { items.retry() },
                )
            },
            loadingContent = {
                Fullscreen.Loading(
                    modifier = modifier,
                    placeholder = placeholder,
                    placeholderContent = itemContent,
                    numberOfPlaceholders = numberOfPlaceholders,
                )
            },
        ) {
            if (items.itemCount == 0) {
                Fullscreen.EmptyList<T>(modifier, emptyListMessage)
            } else {
                SwipeRefreshContent(
                    items = items,
                    modifier = modifier,
                    listState = listState,
                    key = key,
                    placeholder = placeholder,
                    itemContent = itemContent,
                    snackbarHostState = snackbarHostState,
                )
            }
        }
    }
}
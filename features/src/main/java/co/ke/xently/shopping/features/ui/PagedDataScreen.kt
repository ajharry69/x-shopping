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

    @Composable // TODO: Change access visibility to private
    fun Setup(
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
    fun <T : Any> DefaultSetupAppendLoadState(
        items: LazyPagingItems<T>,
        snackbarHostState: SnackbarHostState,
    ) {
        Setup(
            state = items.loadState.append,
            loadingContent = {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally),
                )
            },
            errorContent = {
                val message = it.error.getErrorMessage(LocalContext.current)
                LaunchedEffect(message) {
                    snackbarHostState.showSnackbar(message)
                }
            },
            successContent = {

            },
        )
    }

    @Composable
    fun <T : Any> LazyColumnSuccessContent(
        modifier: Modifier,
        items: LazyPagingItems<T>,
        listState: LazyListState,
        prependContent: LazyListScope.() -> Unit,
        appendLoadingContent: @Composable () -> Unit,
        appendErrorContent: @Composable (LoadState.Error) -> Unit,
        appendSuccessContent: @Composable (LoadState.NotLoading) -> Unit,
    ) {
        LazyColumn(state = listState, modifier = modifier) {
            prependContent()
            item {
                Setup(
                    state = items.loadState.append,
                    loadingContent = appendLoadingContent,
                    errorContent = appendErrorContent,
                    successContent = appendSuccessContent,
                )
            }
        }
    }

    @Composable
    operator fun <T : Any> invoke(
        state: LoadState,
        items: LazyPagingItems<T>,
        loadingContent: @Composable () -> Unit,
        errorContent: @Composable (LoadState.Error) -> Unit,
        successWithEmptyListContent: @Composable (LoadState.NotLoading) -> Unit,
        successWithNonEmptyListContent: @Composable (LoadState.NotLoading) -> Unit,
    ) {
        val successContent by remember(items) {
            derivedStateOf {
                if (items.itemCount == 0) {
                    successWithEmptyListContent
                } else {
                    successWithNonEmptyListContent
                }
            }
        }
        Setup(
            state = state,
            errorContent = errorContent,
            loadingContent = loadingContent,
            successContent = successContent,
        )
    }

    @Composable
    operator fun <T : Any> invoke(
        modifier: Modifier,
        state: LoadState,
        items: LazyPagingItems<T>,
        loadingContent: @Composable () -> Unit,
        errorContent: @Composable (LoadState.Error) -> Unit,
        successWithEmptyListContent: @Composable (LoadState.NotLoading) -> Unit,
        content: @Composable (LoadState.NotLoading) -> Unit,
    ) {
        invoke(
            state = state,
            items = items,
            errorContent = errorContent,
            loadingContent = loadingContent,
            successWithEmptyListContent = successWithEmptyListContent,
        ) {
            SwipeRefreshContent(modifier = modifier, items = items) {
                content(it)
            }
        }
    }

    @Composable
    operator fun <T : Any> invoke(
        modifier: Modifier,
        state: LoadState,
        items: LazyPagingItems<T>,
        listState: LazyListState,
        prependContent: LazyListScope.() -> Unit,
        loadingContent: @Composable () -> Unit,
        errorContent: @Composable (LoadState.Error) -> Unit,
        successWithEmptyListContent: @Composable (LoadState.NotLoading) -> Unit,
        appendLoadingContent: @Composable () -> Unit,
        appendErrorContent: @Composable (LoadState.Error) -> Unit,
        appendSuccessContent: @Composable (LoadState.NotLoading) -> Unit,
    ) {
        invoke(
            state = state,
            items = items,
            modifier = modifier,
            errorContent = errorContent,
            loadingContent = loadingContent,
            successWithEmptyListContent = successWithEmptyListContent,
        ) {
            LazyColumnSuccessContent(
                modifier = Modifier.fillMaxSize(),
                items = items,
                listState = listState,
                prependContent = prependContent,
                appendErrorContent = appendErrorContent,
                appendLoadingContent = appendLoadingContent,
                appendSuccessContent = appendSuccessContent,
            )
        }
    }

    @Composable
    operator fun <T : Any> invoke(
        modifier: Modifier,
        state: LoadState,
        items: LazyPagingItems<T>,
        listState: LazyListState,
        prependContent: LazyListScope.() -> Unit,
        loadingContent: @Composable () -> Unit,
        errorContent: @Composable (LoadState.Error) -> Unit,
        successWithEmptyListContent: @Composable (LoadState.NotLoading) -> Unit,
        appendErrorContent: @Composable (LoadState.Error) -> Unit,
    ) {
        invoke(
            state = state,
            items = items,
            modifier = modifier,
            listState = listState,
            errorContent = errorContent,
            loadingContent = loadingContent,
            prependContent = prependContent,
            appendErrorContent = appendErrorContent,
            successWithEmptyListContent = successWithEmptyListContent,
            appendSuccessContent = {

            },
            appendLoadingContent = {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally),
                )
            },
        )
    }

    @Composable
    operator fun <T : Any> invoke(
        modifier: Modifier,
        state: LoadState,
        items: LazyPagingItems<T>,
        listState: LazyListState,
        snackbarHostState: SnackbarHostState,
        prependContent: LazyListScope.() -> Unit,
        loadingContent: @Composable () -> Unit,
        errorContent: @Composable (LoadState.Error) -> Unit,
        successWithEmptyListContent: @Composable (LoadState.NotLoading) -> Unit,
    ) {
        invoke(
            state = state,
            items = items,
            modifier = modifier,
            listState = listState,
            errorContent = errorContent,
            loadingContent = loadingContent,
            prependContent = prependContent,
            successWithEmptyListContent = successWithEmptyListContent,
            appendErrorContent = {
                val message = it.error.getErrorMessage(LocalContext.current)
                LaunchedEffect(message) {
                    snackbarHostState.showSnackbar(message)
                }
            },
        )
    }

    @Composable
    fun <T : Any> WithoutSwipeRefresh(
        modifier: Modifier,
        state: LoadState,
        items: LazyPagingItems<T>,
        listState: LazyListState,
        prependContent: LazyListScope.() -> Unit,
        loadingContent: @Composable () -> Unit,
        errorContent: @Composable (LoadState.Error) -> Unit,
        successWithEmptyListContent: @Composable (LoadState.NotLoading) -> Unit,
        appendLoadingContent: @Composable () -> Unit,
        appendErrorContent: @Composable (LoadState.Error) -> Unit,
        appendSuccessContent: @Composable (LoadState.NotLoading) -> Unit,
    ) {
        invoke(
            state = state,
            items = items,
            errorContent = errorContent,
            loadingContent = loadingContent,
            successWithEmptyListContent = successWithEmptyListContent,
        ) {
            LazyColumnSuccessContent(
                modifier = modifier,
                items = items,
                listState = listState,
                prependContent = prependContent,
                appendErrorContent = appendErrorContent,
                appendLoadingContent = appendLoadingContent,
                appendSuccessContent = appendSuccessContent,
            )
        }
    }

    @Composable
    fun <T : Any> WithoutSwipeRefresh(
        modifier: Modifier,
        state: LoadState,
        items: LazyPagingItems<T>,
        listState: LazyListState,
        prependContent: LazyListScope.() -> Unit,
        loadingContent: @Composable () -> Unit,
        errorContent: @Composable (LoadState.Error) -> Unit,
        successWithEmptyListContent: @Composable (LoadState.NotLoading) -> Unit,
        appendErrorContent: @Composable (LoadState.Error) -> Unit,
    ) {
        WithoutSwipeRefresh(
            state = state,
            items = items,
            modifier = modifier,
            listState = listState,
            errorContent = errorContent,
            prependContent = prependContent,
            loadingContent = loadingContent,
            appendErrorContent = appendErrorContent,
            successWithEmptyListContent = successWithEmptyListContent,
            appendSuccessContent = {

            },
            appendLoadingContent = {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally),
                )
            },
        )
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
            items = items,
            modifier = modifier,
            listState = listState,
            state = items.loadState.refresh,
            snackbarHostState = snackbarHostState,
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
            successWithEmptyListContent = {
                Fullscreen.EmptyList<T>(modifier, emptyListMessage)
            },
            prependContent = {
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
        )
    }
}
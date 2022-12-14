package co.ke.xently.shopping.features.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import co.ke.xently.shopping.features.ui.pullrefresh.PullRefreshIndicator
import co.ke.xently.shopping.features.ui.pullrefresh.pullRefresh
import co.ke.xently.shopping.features.ui.pullrefresh.rememberPullRefreshState
import co.ke.xently.shopping.libraries.data.source.remote.ExceptionUtils.getErrorMessage

object PagedDataScreen {
    @Composable
    private fun <T : Any> PullRefresh(
        modifier: Modifier,
        items: LazyPagingItems<T>,
        content: @Composable () -> Unit,
    ) {
        val loadState by remember(items) {
            derivedStateOf {
                items.loadState.mediator ?: items.loadState.source
            }
        }
        val refreshing by remember(loadState) {
            derivedStateOf {
                loadState.refresh == LoadState.Loading
            }
        }
        val state = rememberPullRefreshState(refreshing = refreshing, onRefresh = items::refresh)

        Box(modifier then Modifier.pullRefresh(state)) {
            content()

            PullRefreshIndicator(
                state = state,
                refreshing = state.refreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }

    @Composable
    private fun Setup(
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
            PullRefresh(modifier = modifier, items = items) {
                content(it)
            }
        }
    }
}
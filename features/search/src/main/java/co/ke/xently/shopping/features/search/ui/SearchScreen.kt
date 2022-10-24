package co.ke.xently.shopping.features.search.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.ke.xently.shopping.features.search.R
import co.ke.xently.shopping.features.ui.VIEW_SPACE
import co.ke.xently.shopping.features.ui.VIEW_SPACE_HALVED
import kotlinx.coroutines.delay

abstract class SearchScreen<S, T>(@StringRes private val searchHint: Int = R.string.fsearch_hint) {
    sealed class SearchResponse<out T> {
        object InitialResults : SearchResponse<Nothing>()
        data class Suggestions<T>(val data: List<T>) : SearchResponse<List<T>>()
        data class Results<T>(val data: T) : SearchResponse<T>()
        object NoResults : SearchResponse<Nothing>()
    }

    @Stable
    inner class SearchState(
        query: TextFieldValue,
        focused: Boolean,
        searching: Boolean,
        suggestions: List<S>,
        searchResults: List<T>,
    ) {
        var query by mutableStateOf(query)
        var focused by mutableStateOf(focused)
        var searching by mutableStateOf(searching)
        var searchResults by mutableStateOf(searchResults)
        var suggestions by mutableStateOf(suggestions)
        internal var imeActionClicked by mutableStateOf(false)

        val searchResponse
            get() = when {
                !focused && query.text.isBlank() -> {
                    SearchResponse.InitialResults
                }
                focused && query.text.isNotBlank() -> {
                    SearchResponse.Suggestions(suggestions)
                }
                searchResults.isEmpty() -> {
                    SearchResponse.NoResults
                }
                else -> {
                    SearchResponse.Results(searchResults)
                }
            }

        override fun toString(): String {
            return "🚀 State query: $query, focused: $focused, searching: $searching " +
                    "suggestions: ${suggestions.size}, " +
                    "searchResults: ${searchResults.size}, " +
                    " searchResponse: $searchResponse"

        }
    }

    @Composable
    protected fun rememberSearchState(
        query: TextFieldValue = TextFieldValue(""),
        focused: Boolean = false,
        searching: Boolean = false,
        suggestions: List<S> = emptyList(),
        searchResults: List<T> = emptyList(),
    ): SearchState {
        return remember {
            SearchState(
                query = query,
                focused = focused,
                searching = searching,
                suggestions = suggestions,
                searchResults = searchResults,
            )
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    protected fun SearchBar(
        query: TextFieldValue,
        searching: Boolean,
        modifier: Modifier,
        focused: Boolean,
        backgroundColor: Color?,
        focusRequester: FocusRequester,
        onBack: () -> Unit,
        onClearQuery: () -> Unit,
        onSearchImeActionClick: () -> Unit,
        onVoiceSearchRequested: () -> Unit,
        onSearchFocusChange: (Boolean) -> Unit,
        onQueryChange: (TextFieldValue) -> Unit,
    ) {
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val defaultBackgroundColor by TopAppBarDefaults.smallTopAppBarColors().containerColor(0f)

        val background = backgroundColor ?: defaultBackgroundColor
        val contentColor = contentColorFor(background)
        Surface(
            color = background,
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(contentAlignment = Alignment.CenterStart) {
                BasicTextField(
                    value = query,
                    singleLine = true,
                    onValueChange = onQueryChange,
                    cursorBrush = SolidColor(contentColor.copy(alpha = 0.7f)),
                    textStyle = LocalTextStyle.current.copy(color = contentColor),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearchImeActionClick()
                        },
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Search,
                        capitalization = KeyboardCapitalization.None,
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            onSearchFocusChange(it.isFocused)
                        },
                    decorationBox = { innerTextField ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 16.dp),
                        ) {
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    if (!focused) {
                                        onBack()
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.fsearch_content_desc_nav_icon),
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                innerTextField()
                            }
                            when {
                                searching -> {
                                    Spacer(Modifier.width(16.dp))
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                                query.text.isNotEmpty() -> {
                                    IconButton(onClick = onClearQuery) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = stringResource(R.string.fsearch_content_desc_clear),
                                        )
                                    }
                                }
                                else -> {
                                    IconButton(onClick = onVoiceSearchRequested) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = stringResource(R.string.fsearch_content_desc_voice_search),
                                        )
                                    }
                                }
                            }
                        }
                    },
                )

                if (query.text.isEmpty()) {
                    Text(
                        text = stringResource(searchHint),
                        modifier = Modifier.padding(start = 48.dp, end = 8.dp),
                        color = contentColor.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }

    @Composable
    protected operator fun invoke(
        state: SearchState,
        autoFocusSearchField: Boolean,
        snackbarHostState: SnackbarHostState,
        onBack: () -> Unit,
        onQueryChange: (String) -> Unit,
        onSearchImeActionClick: (String) -> Unit,
        backgroundColor: Color? = null,
        showProgressbar: Boolean = false,
        onVoiceSearchRequested: () -> Unit = {},
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val focusManager = LocalFocusManager.current
        LaunchedEffect(state.imeActionClicked) {
            if (state.imeActionClicked) {
                onSearchImeActionClick(state.query.text)
                focusManager.clearFocus()
                state.imeActionClicked = false
            }
        }
        LaunchedEffect(state.query.text) {
            state.searching = true
            delay(100)
            if (!state.imeActionClicked) {
                onQueryChange(state.query.text)
            }
            state.searching = false
        }

        val focusRequester: FocusRequester = remember { FocusRequester() }

        LaunchedEffect(autoFocusSearchField) {
            if (autoFocusSearchField) {
                focusRequester.requestFocus()
            }
        }

        Scaffold(
            content = content,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                Column {
                    SearchBar(
                        modifier = Modifier,
                        query = state.query,
                        searching = state.searching,
                        focused = state.focused,
                        backgroundColor = backgroundColor,
                        focusRequester = focusRequester,
                        onBack = { state.query = TextFieldValue(""); onBack() },
                        onClearQuery = {
                            state.query = TextFieldValue("")
                            focusRequester.requestFocus()
                        },
                        onSearchImeActionClick = { state.imeActionClicked = true },
                        onVoiceSearchRequested = onVoiceSearchRequested,
                        onSearchFocusChange = { state.focused = it },
                        onQueryChange = { state.query = it },
                    )
                    if (showProgressbar) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
            },
        )
    }

    @Composable
    protected fun NoResults(
        modifier: Modifier,
        @StringRes message: Int = R.string.fsearch_no_results,
    ) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(message),
            )
        }
    }

    @Composable
    protected fun Suggestion(suggestion: S, state: SearchState, maxLines: Int = 1) {
        val value = suggestion.suggestionTextValue()
        Surface(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val query = suggestion.queryTextOnSuggestionClick()
                state.query = TextFieldValue(
                    text = query,
                    selection = TextRange(query.length),
                )
                state.imeActionClicked = true // Force trigger FTS
            },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PaddingValues(
                        horizontal = VIEW_SPACE,
                        vertical = VIEW_SPACE_HALVED,
                    )),
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                Text(
                    text = value,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(imageVector = Icons.Default.NorthWest, contentDescription = null)
            }
        }
    }

    protected open fun S.suggestionTextValue(): String = toString()

    protected open fun S.queryTextOnSuggestionClick(): String = suggestionTextValue()
}
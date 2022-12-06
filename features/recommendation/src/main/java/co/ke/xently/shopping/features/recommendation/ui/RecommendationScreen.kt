package co.ke.xently.shopping.features.recommendation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import co.ke.xently.shopping.features.models.MenuItem
import co.ke.xently.shopping.features.recommendation.R
import co.ke.xently.shopping.features.recommendation.models.Recommendation
import co.ke.xently.shopping.features.recommendation.ui.detail.RecommendationDetailScreen
import co.ke.xently.shopping.features.recommendation.ui.shared.ModalBottomSheetLayout
import co.ke.xently.shopping.features.recommendation.ui.shared.ModalBottomSheetValue
import co.ke.xently.shopping.features.recommendation.ui.shared.rememberModalBottomSheetState
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.remote.HttpException
import kotlinx.coroutines.launch

internal object RecommendationScreen {
    data class Config(
        val shared: Shared = Shared(),
        val onDetailClick: (Recommendation) -> Unit = {},
        val onDirectionClick: (Recommendation) -> Unit = {},
    )

    @Composable
    private fun ScreenWithAppBar(
        modifier: Modifier,
        shared: Shared,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        Column(modifier = modifier) {
            TopAppBarWithProgressIndicator(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.toolbar_title_recommendations))
                    },
                    navigationIcon = {
                        IconButton(onClick = shared.onNavigationIconClicked) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    },
                )
            }
            content()
        }
    }

    @Composable
    operator fun invoke(
        modifier: Modifier,
        config: Config,
        viewModel: RecommendationViewModel = hiltViewModel(),
    ) {
        val recommendations by viewModel.recommendations.collectAsState()

        CallOnLifecycleEvent {
            if (it == Lifecycle.Event.ON_START) {
                viewModel.getRecommendation()
            }
        }

        invoke(
            modifier = modifier,
            state = recommendations,
            recommendation = viewModel.recommendation,
            config = config.copy(onDetailClick = viewModel::updateRecommendation),
        )
    }

    @Composable
    operator fun invoke(
        modifier: Modifier,
        config: Config,
        recommendation: Recommendation?,
        state: State<List<Recommendation>>,
    ) {
        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val scope = rememberCoroutineScope()
        ModalBottomSheetLayout(
            sheetState = sheetState,
            sheetShape = MaterialTheme.shapes.large.copy(bottomEnd = CornerSize(0),
                bottomStart = CornerSize(0)),
            sheetContent = {
                AnimatedContent(targetState = recommendation) {
                    if (it == null) {
                        FullscreenLoading<Nothing>(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                        )
                    } else {
                        RecommendationDetailScreen(
                            modifier = Modifier.fillMaxWidth(),
                            recommendation = it,
                        )
                    }
                }
            },
        ) {
            AnimatedContent(targetState = state) {
                when (it) {
                    is State.Error -> {
                        ScreenWithAppBar(modifier = modifier, shared = config.shared) {
                            FullscreenError(
                                error = it.error,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                postErrorContent = { error ->
                                    if (error is HttpException && error.errorCode == "my_location_required") {
                                        Button(onClick = { /*TODO*/ }) {
                                            Text(text = stringResource(R.string.request_location_permission))
                                        }
                                    }
                                },
                            )
                        }
                    }
                    State.Loading -> {
                        ScreenWithAppBar(modifier = modifier, shared = config.shared) {
                            FullscreenLoading<Nothing>(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                            )
                        }
                    }
                    is State.Success -> {
                        ScreenWithAppBar(modifier = modifier, shared = config.shared) {
                            LazyColumn {
                                if (it.data.isNullOrEmpty()) {
                                    item {
                                        FullscreenEmptyList<Unit>(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            error = "",
                                        )
                                    }
                                } else {
                                    items(it.data!!) { recommendation ->
                                        val onClick: (Recommendation) -> Unit = { r ->
                                            scope.launch {
                                                if (sheetState.isVisible) {
                                                    sheetState.hide()
                                                } else {
                                                    config.onDetailClick(r)
                                                    sheetState.show()
                                                }
                                            }
                                        }
                                        RecommendationCardItem(
                                            modifier = Modifier,
                                            recommendation = recommendation,
                                            onClick = onClick,
                                            menuItems = setOf(
                                                MenuItem(
                                                    label = R.string.recommendations_directions,
                                                    onClick = config.onDirectionClick,
                                                ),
                                                MenuItem(
                                                    label = R.string.recommendations_details,
                                                    onClick = onClick,
                                                ),
                                            ),
                                        )
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
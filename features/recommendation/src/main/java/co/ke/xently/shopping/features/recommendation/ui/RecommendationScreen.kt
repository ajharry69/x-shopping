package co.ke.xently.shopping.features.recommendation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import co.ke.xently.shopping.features.map.MapViewWithLoadingIndicator
import co.ke.xently.shopping.features.map.MapViewWithLoadingIndicator.rememberMyUpdatedLocation
import co.ke.xently.shopping.features.map.Permissions
import co.ke.xently.shopping.features.models.MenuItem
import co.ke.xently.shopping.features.recommendation.R
import co.ke.xently.shopping.features.recommendation.models.Recommendation
import co.ke.xently.shopping.features.recommendation.models.RecommendationRequest
import co.ke.xently.shopping.features.recommendation.ui.detail.RecommendationDetailScreen
import co.ke.xently.shopping.features.recommendation.ui.shared.ModalBottomSheetLayout
import co.ke.xently.shopping.features.recommendation.ui.shared.ModalBottomSheetState
import co.ke.xently.shopping.features.recommendation.ui.shared.ModalBottomSheetValue
import co.ke.xently.shopping.features.recommendation.ui.shared.rememberModalBottomSheetState
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.Coordinate
import co.ke.xently.shopping.libraries.data.source.remote.HttpException
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

internal object RecommendationScreen {
    @Stable
    data class Config(
        val shared: Shared = Shared(),
        val onDetailClick: (Recommendation) -> Unit = {},
        val onDirectionClick: (Recommendation) -> Unit = {},
        val getRecommendations: (RecommendationRequest) -> Unit = {},
    )

    @Composable
    private fun SuccessView(
        config: Config,
        modifier: Modifier,
        recommendations: List<Recommendation>?,
        sheetState: ModalBottomSheetState,
        onRetryButtonClick: () -> Unit,
    ) {
        val scope = rememberCoroutineScope()
        val currencyFormat = rememberCurrencyFormat()
        val onRecommendationClick: (Recommendation) -> Unit = { recommendation ->
            scope.launch {
                if (sheetState.isVisible) {
                    sheetState.hide()
                } else {
                    config.onDetailClick(recommendation)
                    sheetState.show()
                }
            }
        }
        val shopLocationCache = remember {
            mutableStateMapOf<Long, LatLng>()
        }
        if (recommendations.isNullOrEmpty()) {
            FullscreenEmptyList<Unit>(
                modifier = modifier,
                error = stringResource(R.string.error_empty_recommendations),
                postContent = {
                    Button(onClick = onRetryButtonClick) {
                        Text(stringResource(R.string.retry).toUpperCase(Locale.current))
                    }
                },
            )
        } else {
            LazyColumn {
                stickyHeader {
                    MapViewWithLoadingIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                    ) {
                        recommendations.forEach { recommendation ->
                            val shop = recommendation.shop
                            val markerState = rememberMarkerState(
                                key = shop.id.toString(),
                                position = shopLocationCache.getOrPut(shop.id) {
                                    LatLng(shop.coordinates.lat, shop.coordinates.lon)
                                },
                            )
                            Marker(
                                state = markerState,
                                title = "${shop.name}, ${shop.taxPin}",
                                snippet = LocalContext.current.resources.getQuantityString(
                                    R.plurals.recommendations_item_description,
                                    recommendation.numberOfItems,
                                    recommendation.hit.count,
                                    recommendation.numberOfItems,
                                    recommendation.expenditure.total.let(currencyFormat::format),
                                ),
                                onInfoWindowClick = {
                                    onRecommendationClick(recommendation)
                                },
                            )
                        }
                    }
                }
                items(recommendations) { recommendation ->
                    RecommendationCardItem(
                        modifier = Modifier,
                        recommendation = recommendation,
                        onClick = onRecommendationClick,
                        menuItems = setOf(
                            MenuItem(
                                label = R.string.recommendations_directions,
                                onClick = config.onDirectionClick,
                            ),
                            MenuItem(
                                label = R.string.recommendations_details,
                                onClick = onRecommendationClick,
                            ),
                        ),
                    )
                }
            }
        }
    }

    @Composable
    private fun LocationRequired(onLocationPermissionGranted: () -> Unit) {
        var shouldRequestPermission by remember {
            mutableStateOf(false)
        }

        Permissions.requestLocationPermission(
            shouldRequestPermission = shouldRequestPermission,
            onLocationPermissionChanged = { granted ->
                if (granted.value) {
                    shouldRequestPermission = false
                    onLocationPermissionGranted()
                }
            },
        )

        Button(onClick = { shouldRequestPermission = true }) {
            Text(text = stringResource(R.string.retry_with_location_captured))
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
            config = config.copy(
                onDetailClick = viewModel::updateRecommendation,
                getRecommendations = viewModel::getRecommendation,
            ),
        )
    }

    @Composable
    operator fun invoke(
        modifier: Modifier,
        config: Config,
        recommendation: Recommendation?,
        state: State<List<Recommendation>>,
    ) {
        val context = LocalContext.current
        var usableState by remember(state) {
            mutableStateOf(state)
        }
        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        var loadingMessage by rememberSaveable {
            mutableStateOf<String?>(null)
        }
        var shouldGetMyCurrentLocationThenFetchRecommendations by rememberSaveable {
            mutableStateOf(false)
        }

        if (shouldGetMyCurrentLocationThenFetchRecommendations) {
            val updatedLocation = rememberMyUpdatedLocation()
            LaunchedEffect(updatedLocation.myLocation) {
                updatedLocation.myLocation?.also { location ->
                    val request = RecommendationRequest(
                        emptyList(),
                        myLocation = Coordinate(
                            lat = location.latitude,
                            lon = location.longitude,
                        ),
                    )
                    config.getRecommendations(request)
                    loadingMessage = null
                    shouldGetMyCurrentLocationThenFetchRecommendations = false
                }
            }
        }
        val onLocationPermissionGranted = {
            usableState = State.Loading
            shouldGetMyCurrentLocationThenFetchRecommendations = true
            loadingMessage = context.getString(R.string.capturing_location)
        }

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
            Column(modifier = modifier) {
                TopAppBarWithProgressIndicator(modifier = Modifier.fillMaxWidth()) {
                    TopAppBar(
                        title = {
                            Text(stringResource(R.string.toolbar_title_recommendations))
                        },
                        navigationIcon = {
                            IconButton(onClick = config.shared.onNavigationIconClicked) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null)
                            }
                        },
                    )
                }

                AnimatedContent(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    targetState = usableState,
                ) {
                    when (it) {
                        is State.Error -> {
                            FullscreenError(
                                error = it.error,
                                modifier = Modifier.fillMaxSize(),
                                postErrorContent = { error ->
                                    if (error is HttpException && error.errorCode == "location_required") {
                                        LocationRequired(onLocationPermissionGranted)
                                    }
                                },
                            )
                        }
                        State.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.semantics {
                                            testTag = TEST_TAG_CIRCULAR_PROGRESS_BAR
                                        },
                                    )

                                    loadingMessage?.also { message ->
                                        Text(message)
                                    }
                                }
                            }
                        }
                        is State.Success -> {
                            SuccessView(
                                recommendations = it.data,
                                config = config,
                                sheetState = sheetState,
                                modifier = Modifier.fillMaxSize(),
                                onRetryButtonClick = onLocationPermissionGranted,
                            )
                        }
                    }
                }
            }
        }
    }
}
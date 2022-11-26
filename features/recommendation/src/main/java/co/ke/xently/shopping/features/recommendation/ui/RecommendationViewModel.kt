package co.ke.xently.shopping.features.recommendation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.recommendation.models.Recommendation
import co.ke.xently.shopping.features.recommendation.models.RecommendationRequest
import co.ke.xently.shopping.features.recommendation.models.mappers.asResource
import co.ke.xently.shopping.features.recommendation.models.mappers.asUi
import co.ke.xently.shopping.features.recommendation.repositories.IRecommendationRepository
import co.ke.xently.shopping.features.recommendation.ui.request.RecommendationRequestViewModel
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.remote.ExceptionUtils.retryCatch
import co.ke.xently.shopping.libraries.data.source.remote.Http
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class RecommendationViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val repository: IRecommendationRepository,
) : RecommendationRequestViewModel(stateHandle, repository) {

    var recommendation by mutableStateOf<Recommendation?>(null)
        private set

    fun updateRecommendation(recommendation: Recommendation? = null) {
        this.recommendation = recommendation
    }
}
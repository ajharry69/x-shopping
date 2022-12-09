package co.ke.xently.shopping.features.recommendation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.ke.xently.shopping.features.recommendation.models.Recommendation
import co.ke.xently.shopping.features.recommendation.models.RecommendationRequest
import co.ke.xently.shopping.features.recommendation.repositories.IRecommendationRepository
import co.ke.xently.shopping.features.recommendation.ui.request.RecommendationRequestViewModel
import co.ke.xently.shopping.features.utils.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class RecommendationViewModel @Inject constructor(
    private val repository: IRecommendationRepository,
) : RecommendationRequestViewModel(repository) {

    var recommendation by mutableStateOf<Recommendation?>(null)
        private set

    fun updateRecommendation(recommendation: Recommendation? = null) {
        this.recommendation = recommendation
    }

    private val _recommendations =
        MutableStateFlow<State<List<Recommendation>>>(State.Success(null))
    val recommendations = _recommendations.asStateFlow()

    private val getRecommendations = MutableSharedFlow<RecommendationRequest>()

    init {
        viewModelScope.launch {
            getRecommendations.collectLatest { recommendationRequest ->
                repository.get(recommendationRequest).transformLatest { result ->
                    result.onSuccess {
                        emit(State.Success(it))
                    }.onFailure {
                        emit(State.Error(it))
                    }
                }.onStart {
                    emit(State.Loading)
                }.collect(_recommendations::emit)
            }
        }
    }

    fun getRecommendation(request: RecommendationRequest) {
        viewModelScope.launch {
            getRecommendations.emit(request)
        }
    }

    fun getRecommendation() {
        getRecommendation(RecommendationRequest(emptyList()))
    }
}
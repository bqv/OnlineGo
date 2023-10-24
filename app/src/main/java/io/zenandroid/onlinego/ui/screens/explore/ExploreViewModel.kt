package io.zenandroid.onlinego.ui.screens.explore

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.zenandroid.onlinego.data.model.local.LadderPlayer
import io.zenandroid.onlinego.data.model.ogs.OGSPlayer
import io.zenandroid.onlinego.data.model.ogs.Ladder
import io.zenandroid.onlinego.data.ogs.OGSRestService
import io.zenandroid.onlinego.data.repositories.LadderRepository
import io.zenandroid.onlinego.data.repositories.SettingsRepository
import io.zenandroid.onlinego.data.repositories.UserSessionRepository
import io.zenandroid.onlinego.gamelogic.Util
import io.zenandroid.onlinego.utils.egfToRank
import io.zenandroid.onlinego.utils.formatRank
import io.zenandroid.onlinego.utils.recordException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExploreViewModel(
  private val restService: OGSRestService,
  private val ladderRepository: LadderRepository,
  private val userSessionRepository: UserSessionRepository,
  private val settingsRepository: SettingsRepository,
) : ViewModel() {
  private val playerId: Long by lazy { userSessionRepository.userId!! }

  val _state: MutableStateFlow<ExploreState> = MutableStateFlow(
    ExploreState.Initial
  )
  val state: StateFlow<ExploreState> get() = _state.asStateFlow()

  init {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        restService.getPlayerProfileAsync(playerId)
          .let(::updatePlayerDetails)
      } catch (t: Throwable) {
        onError(t)
      }
    }

    viewModelScope.launch(Dispatchers.IO) {
      try {
        ladderRepository.getPlayerLadders()
          .catch { onError(it) }
          .map { it.sortedBy { it.name } }
          .collect(::updateLadders)
      } catch (t: Throwable) {
        onError(t)
      }
    }
  }

  private fun updatePlayerDetails(playerDetails: OGSPlayer) {
    _state.update {
      it.copy(
        playerDetails = playerDetails
      )
    }
  }

  private fun updateLadders(ladders: List<Ladder>) {
    _state.update {
      it.copy(
        laddersState = it.laddersState.copy(
          ladders = ladders
        )
      )
    }
  }

  fun openLadder(ladderId: Long) {
    _state.update {
      it.copy(title = "Test $ladderId")
    }
  }

  fun closeLadder() {
    _state.update {
      it.copy(title = null)
    }
  }

  private fun onError(t: Throwable) {
    Log.e("ExploreViewModel", t.message, t)
    io.zenandroid.onlinego.utils.toastException(t)
    recordException(t)
  }
}

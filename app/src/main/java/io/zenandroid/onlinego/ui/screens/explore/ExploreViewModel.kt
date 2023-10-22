package io.zenandroid.onlinego.ui.screens.explore

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.zenandroid.onlinego.data.model.ogs.OGSPlayer
import io.zenandroid.onlinego.data.ogs.OGSRestService
import io.zenandroid.onlinego.data.repositories.LadderRepository
import io.zenandroid.onlinego.data.repositories.SettingsRepository
import io.zenandroid.onlinego.data.repositories.UserSessionRepository
import io.zenandroid.onlinego.gamelogic.Util
import io.zenandroid.onlinego.utils.egfToRank
import io.zenandroid.onlinego.utils.formatRank
import io.zenandroid.onlinego.utils.recordException
import kotlinx.coroutines.flow.MutableStateFlow
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
  private val playerId: Long = userSessionRepository.userId!!

  val state: MutableStateFlow<ExploreState> = MutableStateFlow(
    ExploreState.Initial
  )

  init {
    viewModelScope.launch {
      try {
        fillPlayerDetails(restService.getPlayerProfileAsync(playerId))
      } catch (t: Throwable) {
        onError(t)
      }
    }
  }

  private fun fillPlayerDetails(playerDetails: OGSPlayer) {
    state.update {
      it.copy(playerDetails = playerDetails)
    }
  }

  fun openLadder(ladderId: Long) {
    state.update {
      it.copy(title = "Test $ladderId")
    }
  }

  fun closeLadder() {
    state.update {
      it.copy(title = null)
    }
  }

  private fun onError(t: Throwable) {
    Log.e("ExploreViewModel", t.message, t)
    recordException(t)
  }
}

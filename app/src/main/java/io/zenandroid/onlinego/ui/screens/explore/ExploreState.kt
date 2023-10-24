package io.zenandroid.onlinego.ui.screens.explore

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.zenandroid.onlinego.data.model.BoardTheme
import io.zenandroid.onlinego.data.model.local.LadderPlayer
import io.zenandroid.onlinego.data.model.ogs.Group
import io.zenandroid.onlinego.data.model.ogs.Ladder
import io.zenandroid.onlinego.data.model.ogs.OGSPlayer
import io.zenandroid.onlinego.data.model.ogs.Tournament

data class LaddersState(
  val ladders: List<Ladder> = emptyList()
) {
  val subtitle: String get() = "Ladders"
}

data class TournamentsState(
  val myTournaments: List<Tournament> = emptyList(),

  val liveTournaments: List<Tournament> = emptyList(),
  val correspondenceTournaments: List<Tournament> = emptyList(),
  val archivedTournaments: List<Tournament> = emptyList(),

  val recurringTournaments: List<Tournament> = emptyList(),
) {
  val subtitle: String get() = "Tournaments"
}

data class GroupsState(
  val groups: List<Group> = emptyList(),
) {
  val subtitle: String get() = "Groups"
}

data class ExploreState(
  val title: String? = null,
  val loading: Boolean = false,
  val boardTheme: BoardTheme = BoardTheme.WOOD,
  val playerDetails: OGSPlayer? = null,

  val laddersState: LaddersState = LaddersState(),
  val tournamentsState: TournamentsState = TournamentsState(),
  val groupsState: GroupsState = GroupsState(),
) {
  companion object {
    val Initial = ExploreState()
  }
}

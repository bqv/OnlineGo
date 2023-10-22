package io.zenandroid.onlinego.ui.screens.explore

import androidx.compose.runtime.Immutable
import io.zenandroid.onlinego.data.model.ogs.OGSPlayer

@Immutable
data class LaddersState(
  val placeholder: Nothing? = null
) {
  val subtitle: String get() = "Ladders"
}

@Immutable
data class TournamentsState(
  val placeholder: Nothing? = null
) {
  val subtitle: String get() = "Tournaments"
}

@Immutable
data class GroupsState(
  val placeholder: Nothing? = null
) {
  val subtitle: String get() = "Groups"
}

@Immutable
data class ExploreState(
  val title: String? = null,
  val loading: Boolean = false,
  val playerDetails: OGSPlayer? = null,

  val laddersState: LaddersState = LaddersState(),
  val tournamentsState: TournamentsState = TournamentsState(),
  val groupsState: GroupsState = GroupsState(),
) {
  companion object {
    val Initial = ExploreState()
  }
}

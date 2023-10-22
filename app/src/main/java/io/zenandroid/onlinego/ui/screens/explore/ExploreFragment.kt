package io.zenandroid.onlinego.ui.screens.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.ui.screens.explore.ExploreState
import io.zenandroid.onlinego.ui.theme.OnlineGoTheme
import io.zenandroid.onlinego.utils.analyticsReportScreen
import io.zenandroid.onlinego.utils.rememberStateWithLifecycle
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ExploreFragment : Fragment() {

  private val viewModel: ExploreViewModel by viewModel()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ) = ComposeView(requireContext()).apply {
    setContent {
      OnlineGoTheme {
        val state by rememberStateWithLifecycle(viewModel.state)

        ExploreScreen(
            state = state,
            openLadder = { viewModel.openLadder(it) },
            onBack = { viewModel.closeLadder() },
        )
      }
    }
  }

  override fun onResume() {
    super.onResume()
    analyticsReportScreen("Explore")
  }
}

enum class ExploreTab {
    LADDERS,
    TOURNAMENTS,
    GROUPS;

    companion object {
        private val values = values().associate { it.ordinal to it }
        operator fun get(value: Int) = values[value]
    }
}

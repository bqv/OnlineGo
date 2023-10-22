package io.zenandroid.onlinego.ui.screens.automatch

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.NavOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.data.model.ogs.Size
import io.zenandroid.onlinego.data.model.ogs.Speed
import io.zenandroid.onlinego.ui.screens.main.MainActivity
import io.zenandroid.onlinego.ui.theme.OnlineGoTheme
import io.zenandroid.onlinego.ui.screens.stats.PLAYER_ID
import io.zenandroid.onlinego.utils.rememberStateWithLifecycle
import java.util.Locale
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "NewAutomatchChallengeBS"

class NewAutomatchChallengeBottomSheet : BottomSheetDialogFragment() {
  private val viewModel: NewAutomatchChallengeViewModel by viewModel()

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState)

    dialog.setOnShowListener {
      BottomSheetBehavior.from(dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!)
        .apply {
          state = BottomSheetBehavior.STATE_EXPANDED
          skipCollapsed = true
        }
    }

    return dialog
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        val state by rememberStateWithLifecycle(viewModel.state)

        OnlineGoTheme {
          NewAutomatchChallengeBottomSheetContent(
            state = state,
            onGraphProfileClicked = {
              viewModel.findPlayerByName(it, {
                dismiss()
                navigateToPlayerProfile(it)
              }, {
                Log.e("ChallengeMarkerView", it.toString())
              })
            },
            onGraphChallengeAccepted = {
              viewModel.acceptOpenChallenge(it, {
                dismiss()
              }, {
                Toast.makeText(context, "Not Eligible: ${it.message}",
                  Toast.LENGTH_SHORT).show()
              })
            },
            onSmallCheckChanged = { viewModel.onSmallCheckChanged(it) },
            onMediumCheckChanged = { viewModel.onMediumCheckChanged(it) },
            onLargeCheckChanged = { viewModel.onLargeCheckChanged(it) },
            onSpeedChanged = { viewModel.onSpeedChanged(it) },
            onSearchClicked = {
              dismiss()
              val selectedSizes = mutableListOf<Size>()
              if (state.small) {
                selectedSizes.add(Size.SMALL)
              }
              if (state.medium) {
                selectedSizes.add(Size.MEDIUM)
              }
              if (state.large) {
                selectedSizes.add(Size.LARGE)
              }
              (activity as? MainActivity)?.onAutomatchSearchClicked(state.speed, selectedSizes)
            }
          )
        }
      }
    }
  }

  private fun navigateToPlayerProfile(playerId: Long) {
    (context as FragmentActivity).findNavController(R.id.fragment_container).navigate(
      R.id.stats,
      bundleOf(PLAYER_ID to playerId),
      NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setPopUpTo(R.id.myGames, false, false)
        .build())
  }
}

@Composable
private fun NewAutomatchChallengeBottomSheetContent(
  state: AutomatchState,
  onGraphProfileClicked: (String) -> Unit = {},
  onGraphChallengeAccepted: (Long) -> Unit = {},
  onSmallCheckChanged: (Boolean) -> Unit = {},
  onMediumCheckChanged: (Boolean) -> Unit = {},
  onLargeCheckChanged: (Boolean) -> Unit = {},
  onSpeedChanged: (Speed) -> Unit = {},
  onSearchClicked: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Surface {
    Column(
      modifier
        .padding(16.dp)
    ) {
      Text(text = "Try your hand at a game against a human opponent of similar rating to you.")
      Text(
        text = "Game size",
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
      )
      Row {
        SizeCheckbox(checked = state.small, text = "9×9", onClick = onSmallCheckChanged)
        Spacer(modifier = Modifier.weight(1f))
        SizeCheckbox(checked = state.medium, text = "13×13", onClick = onMediumCheckChanged)
        Spacer(modifier = Modifier.weight(1f))
        SizeCheckbox(checked = state.large, text = "19×19", onClick = onLargeCheckChanged)
      }
      Text(
        text = "Time Controls",
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
      )
      Box {
        var expanded by remember { mutableStateOf(false) }
        Text(
          text = state.speed.getText()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() },
          color = MaterialTheme.colors.primary,
          modifier = Modifier
            .clickable {
              expanded = true
            }
            .padding(top = 4.dp)
            .fillMaxWidth()
        )
        DropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false },
          modifier = Modifier.fillMaxWidth()
        ) {
          Speed.entries.forEach {
            DropdownMenuItem(onClick = {
              expanded = false
              onSpeedChanged(it)
            }) {
              Text(text = it.getText()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() })
            }
          }
        }
      }
      Divider(
        //color = ResourcesCompat.getColor(this.context, android.R.color.black),
        thickness = 1.dp,
        modifier = Modifier
          .fillMaxWidth()
          .padding(0.dp, 10.dp)
      )
      Column(
        modifier = Modifier.padding(0.dp, 4.dp)
      ) {
        Text(
          text = "Challenges",
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colors.primary,
          modifier = Modifier.padding(top = 16.dp)
        )
        ChallengeGraph(
          challenges = state.challenges,
          rating = state.rating,
          onProfile = { onGraphProfileClicked(it) },
          onAccept = { onGraphChallengeAccepted(it) },
          modifier = Modifier.fillMaxWidth()
        )
      }
      Button(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
        enabled = state.isAnySizeSelected,
        onClick = onSearchClicked
      ) {
        Text("Search")
      }
    }
  }
}

@Composable
private fun RowScope.SizeCheckbox(checked: Boolean, text: String, onClick: (Boolean) -> Unit) {
  Checkbox(
    checked = checked,
    colors = CheckboxDefaults.colors(
      checkedColor = MaterialTheme.colors.primary
    ),
    onCheckedChange = onClick
  )
  Text(
    text = text,
    modifier = Modifier
      .align(Alignment.CenterVertically)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) { onClick(!checked) }
  )
}

@Preview(showBackground = true)
@Composable
private fun NewAutomatchChallengeBottomSheetPreview() {
  OnlineGoTheme {
    Box(modifier = Modifier.fillMaxSize())
    NewAutomatchChallengeBottomSheetContent(AutomatchState())
  }
}

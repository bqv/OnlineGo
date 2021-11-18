package io.zenandroid.onlinego.ui.screens.learn

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.lifecycle.asLiveData
import androidx.navigation.findNavController
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.ui.screens.tutorial.TUTORIAL_NAME
import io.zenandroid.onlinego.ui.theme.OnlineGoTheme
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Created by alex on 05/11/2017.
 */
@ExperimentalAnimationApi
@ExperimentalMaterialApi
class LearnFragment : Fragment() {


    private val viewModel: LearnViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.asLiveData().observeAsState()

                state?.let {
                    OnlineGoTheme {
                        Screen(it, ::onAction)
                    }
                }
            }
        }
    }

    private fun onAction(action: LearnAction) {
        when(action) {
            is LearnAction.TutorialClicked -> view?.findNavController()?.navigate(R.id.action_learnFragment_to_tutorialFragment, bundleOf(TUTORIAL_NAME to action.tutorial.name))
            LearnAction.JosekiExplorerClicked -> view?.findNavController()?.navigate(R.id.action_learnFragment_to_josekiExplorerFragment)
            LearnAction.PuzzlesClicked -> view?.findNavController()?.navigate(R.id.action_learnFragment_to_puzzleDirectoryFragment)
            else -> viewModel.onAction(action)
        }
    }

    override fun onResume() {
        super.onResume()
      //analytics.setCurrentScreen(requireActivity(), javaClass.simpleName, null)
    }
}

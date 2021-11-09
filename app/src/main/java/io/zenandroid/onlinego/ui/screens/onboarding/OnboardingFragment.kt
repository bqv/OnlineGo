package io.zenandroid.onlinego.ui.screens.onboarding

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.data.repositories.UserSessionRepository
import io.zenandroid.onlinego.ui.screens.onboarding.OnboardingAction.BackPressed
import io.zenandroid.onlinego.ui.screens.onboarding.OnboardingAction.SocialPlatformLoginFailed
import io.zenandroid.onlinego.ui.screens.onboarding.Page.LoginMethod
import io.zenandroid.onlinego.ui.theme.OnlineGoTheme
import io.zenandroid.onlinego.utils.addToDisposable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingFragment : Fragment() {
    private val viewModel: OnboardingViewModel by viewModel()

    private val userSessionRepository: UserSessionRepository by inject()
    private val client = OkHttpClient.Builder()
        .cookieJar(userSessionRepository.cookieJar)
        .followRedirects(false)
        .build()
    private val subscriptions = CompositeDisposable()

    @ExperimentalPagerApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.observeAsState()

                state?.let {
                    when {
                        state?.finish == true -> {
                            requireActivity().finish()
                        }
                        state?.loginSuccessful == true -> {
                            findNavController().navigate(R.id.onboarding_to_mygames)
                        }
                        else -> {
                            OnlineGoTheme {
                                Screen(it, viewModel::onAction)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onAction(BackPressed)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        subscriptions.clear()
    }
}

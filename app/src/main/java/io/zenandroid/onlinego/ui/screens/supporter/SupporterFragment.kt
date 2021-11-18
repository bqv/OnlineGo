package io.zenandroid.onlinego.ui.screens.supporter

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.databinding.FragmentSupporterBinding
import io.zenandroid.onlinego.utils.showIf
import org.koin.android.ext.android.get

class SupporterFragment : Fragment(), SupporterContract.View {

    private lateinit var presenter: SupporterContract.Presenter
    private var analytics = OnlineGoApplication.instance.analytics
    private lateinit var binding: FragmentSupporterBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSupporterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            subscribeButton.setOnClickListener {
                presenter.onSubscribeClick()
              //FirebaseAnalytics.getInstance(requireContext()).logEvent("start_subscription_flow", null)
            }
            backButton.setOnClickListener { activity?.onBackPressed() }
            scrollView.viewTreeObserver.addOnScrollChangedListener {
                if (scrollView != null) {
                    if (scrollView.scrollY == 0) {
                        scrollView.elevation = 1 * Resources.getSystem().displayMetrics.density
                    } else {
                        scrollView.elevation = 0f
                    }
                }
            }
            amountSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    presenter.onUserDragSlider(value)
                }
            }
            cancelButton.setOnClickListener {
                requireActivity().startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = "https://play.google.com/store/account/subscriptions?package=io.zenandroid.onlinego".toUri()
                })
              //FirebaseAnalytics.getInstance(requireContext()).logEvent("cancel_subscription", null)
            }
        }
        presenter = SupporterPresenter(this, get())

      //FirebaseAnalytics.getInstance(requireContext()).setCurrentScreen(requireActivity(), javaClass.simpleName, javaClass.simpleName)
    }

    override fun onResume() {
        super.onResume()
        analytics.setCurrentScreen(requireActivity(), javaClass.simpleName, javaClass.simpleName)
        presenter.subscribe()
    }

    override fun onPause() {
        super.onPause()
        presenter.unsubscribe()
    }

    override fun showError(t: Throwable) {
      //FirebaseCrashlytics.getInstance().recordException(t)
        Toast.makeText(requireContext(), t.message, Toast.LENGTH_LONG).show()
    }

    override fun renderState(state: State) {
        binding.apply {
            state.numberOfTiers?.let {
                amountSlider.valueTo = it.toFloat() - 1
            }

            state.selectedTier?.let {
                if (amountSlider.value.toInt() != it) {
                    amountSlider.value = it.toFloat()
                }
                valueLabel.text = state.skus?.get(it)?.price ?: ""
            }

            state.skus?.let {
                amountSlider.setLabelFormatter { index -> it[index.toInt()].price }
            }

            state.subscribeTitleText?.let {
                subscribeTitle.text = it
            }

            state.subscribeButtonText?.let {
                subscribeButton.text = it
            }

            subscribeButton.isEnabled = state.subscribeButtonEnabled
            subscribeButton.alpha = if (state.subscribeButtonEnabled) 1f else .3f

            loadingProgressView.showIf(state.loading)
            loadingProgressScrim.showIf(state.loading)

            cancelButton.showIf(state.supporter)
        }
    }
}

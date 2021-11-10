package io.zenandroid.onlinego.ui.screens.supporter

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.databinding.FragmentSupporterBinding
import io.zenandroid.onlinego.utils.showIf
import org.koin.android.ext.android.get

class SupporterFragment : Fragment(), SupporterContract.View {

    private lateinit var presenter: SupporterContract.Presenter
    private lateinit var binding: FragmentSupporterBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSupporterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
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
        }
        presenter = SupporterPresenter(this)
    }

    override fun onResume() {
        super.onResume()
        presenter.subscribe()
    }

    override fun onPause() {
        super.onPause()
        presenter.unsubscribe()
    }

    override fun showError(t: Throwable) {
        Log.e("SupporterFragment", t.message, t)
        Toast.makeText(requireContext(), t.message, Toast.LENGTH_LONG).show()
    }

    override fun renderState(state: State) {
        binding.apply {
        }
    }
}

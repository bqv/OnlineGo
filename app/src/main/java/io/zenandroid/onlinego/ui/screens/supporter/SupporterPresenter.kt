package io.zenandroid.onlinego.ui.screens.supporter

import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.zenandroid.onlinego.utils.addToDisposable

class SupporterPresenter(
        private val view: SupporterContract.View
) : SupporterContract.Presenter {

    private val disposables = CompositeDisposable()
    private var state = State(
            loading = true
    )

    override fun subscribe() {
        view.renderState(state)
    }

    override fun unsubscribe() {
        disposables.clear()
    }
}

data class State(
        val loading: Boolean = false,
        val supporterLabelText: String? = null,
        val supporterButtonText: String? = null,
        val subscribeTitleText: CharSequence? = null,
        val subscribeButtonText: String? = null,
        val subscribeButtonEnabled: Boolean = false,
)

package io.zenandroid.onlinego.ui.screens.automatch

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.button.MaterialButton
import io.zenandroid.onlinego.data.model.ogs.SeekGraphChallenge
import io.zenandroid.onlinego.ui.views.ClickableMarkerView
import io.zenandroid.onlinego.utils.formatMillis
import io.zenandroid.onlinego.utils.formatRank
import io.zenandroid.onlinego.utils.timeControlDescription
import io.zenandroid.onlinego.R

class ChallengeMarkerView(
    context: Context,
    val currentRating: Int,
    onProfile: (String) -> Unit,
    onAccept: (Long) -> Unit
) : ClickableMarkerView(context, R.layout.challenge_markerview) {
    private val containerView: LinearLayout = findViewById(R.id.containerView)
    private val rankTextView: TextView = findViewById(R.id.rankTextView)
    private val tpmTextView: TextView = findViewById(R.id.tpmTextView)
    private val userTextView: TextView = findViewById(R.id.userTextView)
    private val profileButton: MaterialButton = findViewById(R.id.profileButton)
    private val acceptButton: MaterialButton = findViewById(R.id.acceptButton)

    lateinit var challenge: SeekGraphChallenge
        private set

    init {
        listOf(profileButton, acceptButton).forEach {
            it.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    view.performClick()
                }
                true
            }
        }
        profileButton.setOnClickListener {
            onProfile(this.challenge.username)
        }
        acceptButton.setOnClickListener {
            onAccept(this.challenge.challenge_id!!)
        }
    }

    override fun onClick(event: MotionEvent) = containerView.dispatchTouchEvent(event)

    // runs every time the MarkerView is redrawn
    override fun refreshContent(e: Entry, highlight: Highlight) {
        (e.data as? SeekGraphChallenge)?.let { challenge = it }
        challenge.let {
            val timePerMove = it.time_per_move?.toLong()?.times(1000)?.let(::formatMillis) ?: ""
            val params = it.time_control_parameters?.let(::timeControlDescription)
            val size = "${it.width}x${it.height}"
            val ranked = "${if (it.ranked) "R" else "Unr"}anked"
            val handicap = "${if (it.handicap == 0) "no" else it.handicap.toString()} handicap"
            val minRank = formatRank(it.min_rank).let { if (it == "") null else ">=$it" }
            val maxRank = formatRank(it.max_rank).let { if (it == "") null else "<=$it" }
            val ranks = when {
                minRank != null && maxRank != null -> "$minRank and $maxRank"
                minRank != null -> maxRank
                maxRank != null -> minRank
                else -> "any"
            }
            acceptButton.visibility = when {
                it.ranked && (e.y - currentRating) > 9 -> View.INVISIBLE
                it.min_rank > currentRating -> View.INVISIBLE
                it.max_rank < currentRating -> View.INVISIBLE
                it.rengo -> View.INVISIBLE
                else -> View.VISIBLE
            }
            rankTextView.text = "${it.username} [${formatRank(it.rank)}]"
            tpmTextView.text = "~$timePerMove / move"
            userTextView.text = "\"${it.name}\": $ranked $size, $handicap, $params\nRanks: $ranks"
        }

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width.toFloat() / 2), -height.toFloat() - 10)
    }
}

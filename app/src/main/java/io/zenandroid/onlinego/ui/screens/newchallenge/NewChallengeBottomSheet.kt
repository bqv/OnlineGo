package io.zenandroid.onlinego.ui.screens.newchallenge

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.data.model.local.Player
import io.zenandroid.onlinego.data.model.ogs.OGSPlayer
import io.zenandroid.onlinego.data.ogs.TimeControl
import io.zenandroid.onlinego.databinding.BottomSheetNewChallengeBinding
import io.zenandroid.onlinego.ui.screens.main.MainActivity
import io.zenandroid.onlinego.ui.screens.newchallenge.selectopponent.SelectOpponentDialog
import io.zenandroid.onlinego.utils.egfToRank
import io.zenandroid.onlinego.utils.formatRank

class NewChallengeBottomSheet : BottomSheetDialogFragment() {

    private val PARAMS_KEY = "PARAMS"
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val challengeParamsAdapter = moshi.adapter(ChallengeParams::class.java)
    private val opponentAdapter = moshi.adapter(Player::class.java)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(OnlineGoApplication.instance)
    private val challenge: ChallengeParams = getSavedChallengeParams()
    private var opponent: Player? = null
    private lateinit var binding: BottomSheetNewChallengeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomSheetNewChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        challenge.opponent = null
        binding.apply {
            botView.apply {
                name = "Opponent"
                value = challenge.opponent?.let {
                    "${it.username} (${formatRank(egfToRank(it.ratings?.overall?.rating))})"
                } ?: "[Open Offer]"
                setOnClickListener {
                    fragmentManager?.let {
                        SelectOpponentDialog().apply {
                            setTargetFragment(this@NewChallengeBottomSheet, 1)
                            show(it, "SELECT_OPPONENT")
                        }
                    }
                }
            }
            colorView.apply {
                name = "You play"
                value = challenge.color
                valuesCallback = { listOf("Auto", "Black", "White") }
            }
            sizeView.apply {
                name = "Size"
                value = challenge.size
                valuesCallback = { listOf("9x9", "13x13", "19x19") }
            }
            handicapView.apply {
                name = "Handicap"
                value = challenge.handicap
                valuesCallback = { listOf("Auto", "0", "1", "2", "3", "4", "5") }
            }
            speedView.apply {
                name = "Speed"
                value = challenge.speed
                valuesCallback = { listOf("Blitz", "Live", "Correspondence") }
                onChangeCallback = {
                    when(it.lowercase()) {
                        "correspondence" -> run {
                            maintimeView.value = 604800
                            periodtimeView.value = 86400 // if canadian, 604800
                            initialtimeView.value = 86400*3
                            maxtimeView.value = 604800
                            timeincrementView.value = 86400
                            permoveView.value = 86400*2
                            stonesperperiodView.value = 10
                            totaltimeView.value = 86400*28
                        }
                        "live" -> run {
                            maintimeView.value = 600
                            periodtimeView.value = 30 // if canadian, 180
                            initialtimeView.value = 120
                            maxtimeView.value = 300
                            timeincrementView.value = 30
                            permoveView.value = 1
                            stonesperperiodView.value = 10
                            totaltimeView.value = 900
                        }
                        "blitz" -> run {
                            maintimeView.value = 30
                            periodtimeView.value = 5 // if canadian, 30
                            initialtimeView.value = 30
                            maxtimeView.value = 60
                            timeincrementView.value = 10
                            permoveView.value = 5
                            stonesperperiodView.value = 5
                            totaltimeView.value = 300
                        }
                        else -> {}
                    }
                    timecontrolView.value = "Byo-Yomi"
                    systemView.value = timecontrolView.value
                    periodsView.value = 5
                }
                onChangeCallback?.invoke(value)
            }
            systemView.apply {
                name = "Time System"
                visibility = View.GONE
            }
            timecontrolView.apply {
                name = "Time Control"
                valuesCallback = { listOf(
                    "Fischer", // Initial, Increment, Max Time
                    "Byo-Yomi", // Main, Period, Periods
                    "Canadian", // Main, Period, Stones per period
                    "Absolute", // Total time
                    "Simple", // Time per move
                    "None") } // Pause on weekends ^^^
                onChangeCallback = {
                    val it = it.lowercase().replace("-", "")
                    maintimeView.visibility = when(it) {
                        "byoyomi", "canadian" -> View.VISIBLE
                        else -> View.GONE
                    }
                    periodtimeView.visibility = when(it) {
                        "byoyomi", "canadian" -> View.VISIBLE
                        else -> View.GONE
                    }
                    periodsView.visibility = when(it) {
                        "byoyomi" -> View.VISIBLE
                        else -> View.GONE
                    }
                    initialtimeView.visibility = when(it) {
                        "fischer" -> View.VISIBLE
                        else -> View.GONE
                    }
                    maxtimeView.visibility = when(it) {
                        "fischer" -> View.VISIBLE
                        else -> View.GONE
                    }
                    timeincrementView.visibility = when(it) {
                        "fischer" -> View.VISIBLE
                        else -> View.GONE
                    }
                    permoveView.visibility = when(it) {
                        "simple" -> View.VISIBLE
                        else -> View.GONE
                    }
                    stonesperperiodView.visibility = when(it) {
                        "canadian" -> View.VISIBLE
                        else -> View.GONE
                    }
                    totaltimeView.visibility = when(it) {
                        "absolute" -> View.VISIBLE
                        else -> View.GONE
                    }
                }
                onChangeCallback?.invoke(value)
            }
            pauseonweekendsView.apply {
                name = "Pause Weekends"
                value = "Yes"
                valuesCallback = { listOf("Yes", "No") }
            }
            maintimeView.apply {
                name = "Main Time (sec)"
            }
            periodtimeView.apply {
                name = "Time / Period (sec)"
            }
            initialtimeView.apply {
                name = "Initial Time (sec)"
            }
            maxtimeView.apply {
                name = "Max Time (sec)"
            }
            timeincrementView.apply {
                name = "Time Increment (sec)"
            }
            permoveView.apply {
                name = "Time / Move (sec)"
            }
            totaltimeView.apply {
                name = "Total Time (sec)"
            }
            periodsView.apply {
                name = "Nr. Periods"
            }
            stonesperperiodView.apply {
                name = "Stones / Period"
            }
            rankedView.apply {
                name = "Ranked"
                value = if (challenge.ranked) "Yes" else "No"
                valuesCallback = { listOf("Yes", "No") }
            }
            disableAnalysisView.apply {
                name = "Analysis"
                value = if (challenge.disable_analysis) "Disabled" else "Enabled"
                valuesCallback = { listOf("Enabled", "Disabled") }
            }
            privateView.apply {
                name = "Private"
                value = if (challenge.private) "Yes" else "No"
                valuesCallback = { listOf("Yes", "No") }
            }
            searchButton.setOnClickListener { this@NewChallengeBottomSheet.onSearchClicked() }

            isCancelable = true
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            BottomSheetBehavior.from(dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
            }
        }

        return dialog
    }

    private fun onSearchClicked() {
        challenge.apply {
            opponent = this@NewChallengeBottomSheet.opponent?.let(OGSPlayer.Companion::fromPlayer)
            color = binding.colorView.value
            handicap = binding.handicapView.value
            ranked = binding.rankedView.value == "Yes"
            size = binding.sizeView.value
            speed = binding.speedView.value
            disable_analysis = binding.disableAnalysisView.value == "Disabled"
            private = binding.privateView.value == "Yes"
            timeControl = when(binding.timecontrolView.value.lowercase().replace("-", "")) {
                "fischer" -> TimeControl(
                    system = "fischer",
                    time_control = "fischer",
                    speed = binding.speedView.value,
                    initial_time = binding.initialtimeView.value,
                    max_time = binding.maxtimeView.value,
                    time_increment = binding.timeincrementView.value,
                    pause_on_weekends = binding.pauseonweekendsView.value == "Yes",
                )
                "byoyomi" -> TimeControl(
                    system = "byoyomi",
                    time_control = "byoyomi",
                    speed = binding.speedView.value,
                    main_time = binding.maintimeView.value,
                    period_time = binding.periodtimeView.value,
                    periods = binding.periodsView.value,
                    pause_on_weekends = binding.pauseonweekendsView.value == "Yes",
                )
                "canadian" -> TimeControl(
                    system = "canadian",
                    time_control = "canadian",
                    speed = binding.speedView.value,
                    main_time = binding.maintimeView.value,
                    period_time = binding.periodtimeView.value,
                    stones_per_period = binding.stonesperperiodView.value,
                    pause_on_weekends = binding.pauseonweekendsView.value == "Yes",
                )
                "absolute" -> TimeControl(
                    system = "absolute",
                    time_control = "absolute",
                    speed = binding.speedView.value,
                    total_time = binding.totaltimeView.value,
                    pause_on_weekends = binding.pauseonweekendsView.value == "Yes",
                )
                "simple" -> TimeControl(
                    system = "simple",
                    time_control = "simple",
                    speed = binding.speedView.value,
                    per_move = binding.permoveView.value,
                    pause_on_weekends = binding.pauseonweekendsView.value == "Yes",
                )
                "none" -> TimeControl(
                    system = "none",
                    time_control = "none",
                    speed = binding.speedView.value,
                    pause_on_weekends = binding.pauseonweekendsView.value == "Yes",
                )
                else -> TimeControl()
            }
        }
        dismiss()
        saveSettings()
        (activity as? MainActivity)?.onNewChallengeSearchClicked(challenge)
    }

    private fun getSavedChallengeParams() =
            prefs.getString(PARAMS_KEY, null)?.let ( challengeParamsAdapter::fromJson )
                    ?: ChallengeParams(
                            opponent = null,
                            color = "Auto",
                            ranked = true,
                            handicap = "0",
                            size = "9x9",
                            speed = "Live",
                            disable_analysis = false,
                            private = false,
                            timeControl = TimeControl()
                    )

    private fun saveSettings() {
        prefs.edit()
                .putString(PARAMS_KEY, challengeParamsAdapter.toJson(challenge))
                .apply()
    }

    private fun selectOpponent(opponent: Player?) {
        binding.botView.value = opponent
                ?.let {"${it.username} (${formatRank(egfToRank(it.rating))})"}
                ?: "[Open Offer]"
        this.opponent = opponent
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 1 && resultCode == RESULT_OK) {
            data?.getStringExtra("OPPONENT")?.let {
                selectOpponent(opponentAdapter.fromJson(it))
            }
        }
    }
}

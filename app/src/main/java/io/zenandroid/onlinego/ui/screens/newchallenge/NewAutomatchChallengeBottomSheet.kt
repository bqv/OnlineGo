package io.zenandroid.onlinego.ui.screens.newchallenge

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.zenandroid.onlinego.data.model.ogs.SeekGraphChallenge
import io.zenandroid.onlinego.data.model.ogs.Size
import io.zenandroid.onlinego.data.model.ogs.Speed
import io.zenandroid.onlinego.data.ogs.OGSWebSocketService
import io.zenandroid.onlinego.data.repositories.UserSessionRepository
import io.zenandroid.onlinego.data.repositories.SeekGraphRepository
import io.zenandroid.onlinego.databinding.BottomSheetNewAutomatchBinding
import io.zenandroid.onlinego.utils.addToDisposable
import io.zenandroid.onlinego.R
import org.koin.core.context.GlobalContext.get
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import androidx.core.os.bundleOf
import io.zenandroid.onlinego.ui.screens.stats.PLAYER_ID
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.BubbleChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.LimitLine.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.data.BubbleDataSet
import com.github.mikephil.charting.data.BubbleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.EntryXComparator
import com.github.mikephil.charting.utils.MPPointF

private const val TAG = "NewAutomatchChallengeBottomSheet"

class NewAutomatchChallengeBottomSheet(context: Context, private val onSearch: (Speed, List<Size>) -> Unit) : BottomSheetDialog(context), OnChartValueSelectedListener {
    companion object {
        private const val SEARCH_GAME_SMALL = "SEARCH_GAME_SMALL"
        private const val SEARCH_GAME_MEDIUM = "SEARCH_GAME_MEDIUM"
        private const val SEARCH_GAME_LARGE = "SEARCH_GAME_LARGE"
        private const val SEARCH_GAME_SPEED = "SEARCH_GAME_SPEED"
    }

    private var selectedSpeed: Speed = Speed.NORMAL
    private val speedsArray = arrayOf(Speed.BLITZ, Speed.NORMAL, Speed.LONG)
    private val currentRating = get().get<UserSessionRepository>().userRating
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private lateinit var binding: BottomSheetNewAutomatchBinding

    private val subscriptions = CompositeDisposable()

    private val chart: BubbleChart
		get() = findViewById(R.id.challengesChart)!!

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = BottomSheetNewAutomatchBinding.inflate(inflater)

        setContentView(binding.root)

        setInitialState()

        binding.searchButton.setOnClickListener {
            dismiss()
            val selectedSizes = mutableListOf<Size>()
            if(binding.smallGameCheckbox.isChecked) {
                selectedSizes.add(Size.SMALL)
            }
            if(binding.mediumGameCheckbox.isChecked) {
                selectedSizes.add(Size.MEDIUM)
            }
            if(binding.largeGameCheckbox.isChecked) {
                selectedSizes.add(Size.LARGE)
            }
            onSearch.invoke(selectedSpeed, selectedSizes)
        }

        arrayOf(binding.smallGameCheckbox, binding.mediumGameCheckbox, binding.largeGameCheckbox).forEach {
            it.setOnCheckedChangeListener { _, _ ->
                onSizeChanged()
            }
        }

        val stringsArray = arrayOfNulls<CharSequence?>(speedsArray.size)
        speedsArray.forEachIndexed { index, speed ->
            stringsArray[index] = speed.getText().capitalize()
        }

        binding.speedRow.setOnClickListener {
            AlertDialog.Builder(context).setTitle("Choose speed")
                    .setItems(stringsArray) { _, which ->
                        selectedSpeed = speedsArray[which]
                        binding.speedTextView.text = selectedSpeed.getText().capitalize()
                        onSpeedChanged()
                    }
                    .setCancelable(true)
                    .create()
                    .show()
        }
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setOnShowListener {
            BottomSheetBehavior.from(findViewById(com.google.android.material.R.id.design_bottom_sheet)!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
            }
        }

        chart.apply {
            getDescription().setEnabled(false)
            setOnChartValueSelectedListener(this@NewAutomatchChallengeBottomSheet)
            setDrawGridBackground(false)
            setTouchEnabled(true)

            // enable scaling and dragging
            setDragEnabled(true)
            setScaleEnabled(true)

            setMaxVisibleValueCount(200)
            setPinchZoom(true)

            // create a dataset and give it a type
            val set1 = BubbleDataSet(ArrayList<BubbleEntry>(), "19x19")
          //set1.setDrawIcons(false)
            set1.setColor(ColorTemplate.COLORFUL_COLORS[0], 130)
            set1.setDrawValues(true)
            set1.setNormalizeSizeEnabled(false)

            val set2 = BubbleDataSet(ArrayList<BubbleEntry>(), "13x13")
          //set2.setDrawIcons(false)
          //set2.setIconsOffset(MPPointF(0f, 15f))
            set2.setColor(ColorTemplate.COLORFUL_COLORS[1], 130)
            set2.setDrawValues(true)
            set2.setNormalizeSizeEnabled(false)

            val set3 = BubbleDataSet(ArrayList<BubbleEntry>(), "9x9")
            set3.setColor(ColorTemplate.COLORFUL_COLORS[2], 130)
            set3.setDrawValues(true)
            set3.setNormalizeSizeEnabled(false)

            val set4 = BubbleDataSet(ArrayList<BubbleEntry>(), "?x?")
            set4.setColor(ColorTemplate.COLORFUL_COLORS[3], 130)
            set4.setDrawValues(true)
            set4.setNormalizeSizeEnabled(false)

            val set5 = BubbleDataSet(ArrayList<BubbleEntry>(), "Eligible")
            set5.setDrawIcons(false)
            set5.setColor(android.graphics.Color.BLUE, 130)
            set5.setDrawValues(true)
            set5.setNormalizeSizeEnabled(false)

            val dataSets = ArrayList<IBubbleDataSet>()
            dataSets.add(set1) // add the data sets
            dataSets.add(set2)
            dataSets.add(set3)
            dataSets.add(set4)
            dataSets.add(set5)

            // create a data object with the data sets
            val data = BubbleData(dataSets)
            data.setDrawValues(false)
            data.setValueTextSize(8f)
            data.setValueTextColor(Color.WHITE)
            data.setHighlightCircleWidth(1.5f)

            chart.setData(data)
            chart.invalidate()

            getLegend().apply {
                setVerticalAlignment(Legend.LegendVerticalAlignment.TOP)
                setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT)
                setOrientation(Legend.LegendOrientation.VERTICAL)
                setDrawInside(false)
                textColor = ResourcesCompat.getColor(resources, R.color.colorText, context.theme)
            }

            getAxisLeft().apply {
                setSpaceTop(30f)
                setSpaceBottom(30f)
                setDrawZeroLine(false)
                setLabelCount(10, true)
                setAxisMinValue(-1f)
                setAxisMaxValue(38f)
                setGranularityEnabled(true)
                setGranularity(1f)
                setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val rank = value.toInt()
                        return when {
                            rank < 30 -> "${30 - rank}k"
                            else -> "${rank - 29}d"
                        }
                    }
                })
                textColor = ResourcesCompat.getColor(resources, R.color.colorText, context.theme)
                currentRating?.toFloat()?.let {
                    addLimitLine(LimitLine(it, "").apply {
                        setLineWidth(.5f)
                        setLineColor(android.graphics.Color.WHITE)
                        setLabelPosition(LimitLabelPosition.RIGHT_TOP)
                        setTextSize(10f)
                    })
                }
            }

            getAxisRight().setEnabled(false)

            getXAxis().apply {
                setPosition(XAxis.XAxisPosition.BOTTOM)
                setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String
                        = when(value.toInt()) {
                            0 -> "Blitz"
                            3 -> "Live"
                            6 -> "Correspondence"
                            else -> ""
                        }
                })
              //setLabelCount(4, true)
                setCenterAxisLabels(true)
                setLabelRotationAngle(9f)
                setAxisMinValue(0f)
                setAxisMaxValue(8f)
                setGranularityEnabled(true)
                setGranularity(1f)
                textColor = ResourcesCompat.getColor(resources, R.color.colorText, context.theme)
                addLimitLine(LimitLine(2f, "").apply {
                    setLineWidth(1.5f)
                    setLineColor(android.graphics.Color.GRAY)
                    setLabelPosition(LimitLabelPosition.RIGHT_TOP)
                    setTextSize(10f)
                })
                addLimitLine(LimitLine(5f, "").apply {
                    setLineWidth(1.5f)
                    setLineColor(android.graphics.Color.GRAY)
                    setLabelPosition(LimitLabelPosition.RIGHT_TOP)
                    setTextSize(10f)
                })
            }

            setNoDataTextColor(ResourcesCompat.getColor(resources, R.color.colorActionableText, context.theme))

            let { chart ->
                // create a custom MarkerView (extend MarkerView) and specify the layout to use for it
                val mv = ChallengeMarkerView(context, {
                    dismiss()
                    (context as FragmentActivity).findNavController(R.id.fragment_container)?.apply {
                        navigate(
                            R.id.stats,
                            bundleOf(PLAYER_ID to it.id),
                            NavOptions.Builder()
                                .setLaunchSingleTop(true)
                                .setPopUpTo(R.id.myGames, false, false)
                                .build())
                    }
                }, {
                    dismiss()
                })
                mv.setChartView(chart)
                chart.setMarker(mv)

                get().get<SeekGraphRepository>().challengesSubject
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ challenges ->
                        chart.getData().let { data ->
                            data.getDataSetByIndex(0).clear()
                            data.getDataSetByIndex(1).clear()
                            data.getDataSetByIndex(2).clear()
                            data.getDataSetByIndex(3).clear()
                            data.getDataSetByIndex(4).clear()
                            challenges
                                .sortedBy { it.time_per_move }
                                .forEach {
                                    val rankDiff = (it.rank ?: 0.0) - (currentRating ?: 0).toDouble()
                                    val drawable = when {
                                        it.ranked && Math.abs(rankDiff) > 9 -> null
                                        (currentRating ?: 0) < it.min_rank -> null
                                        (currentRating ?: 0) > it.max_rank -> null
                                        else -> getResources().getDrawable(R.drawable.ic_star)
                                    }
                                    val dataset = when {
                                        drawable != null -> 4
                                        it.width == 19 -> 0
                                        it.width == 13 -> 1
                                        it.width == 9 -> 2
                                        else -> 3
                                    }
                                    val entry = BubbleEntry(
                                            Math.log10((it.time_per_move ?: 0.0) + 1).toFloat(),
                                            it.rank?.toFloat() ?: 0f,
                                            .2f, drawable, it)
                                    data.addEntry(entry, dataset)
                                }
                            data.notifyDataChanged()
                        }
                        chart.notifyDataSetChanged()
                        chart.invalidate()
                        Log.d("SeekGraph", "${challenges}")
                    }, {
                        Log.e("SeekGraph", it.toString())
                    })
                    .addToDisposable(subscriptions)
            }
        }
    }

    private fun setInitialState() {
        binding.smallGameCheckbox.isChecked = prefs.getBoolean(SEARCH_GAME_SMALL, true)
        binding.mediumGameCheckbox.isChecked = prefs.getBoolean(SEARCH_GAME_MEDIUM, false)
        binding.largeGameCheckbox.isChecked = prefs.getBoolean(SEARCH_GAME_LARGE, false)
        selectedSpeed = Speed.valueOf(prefs.getString(SEARCH_GAME_SPEED, null) ?: Speed.NORMAL.toString())
        binding.speedTextView.text = selectedSpeed.getText().capitalize()
    }

    private fun onSizeChanged() {
        binding.searchButton.isEnabled = binding.smallGameCheckbox.isChecked || binding.mediumGameCheckbox.isChecked || binding.largeGameCheckbox.isChecked
        saveSettings()
    }

    private fun onSpeedChanged() {
        saveSettings()
    }

    private fun saveSettings() {
        prefs.edit()
                .putBoolean(SEARCH_GAME_SMALL, binding.smallGameCheckbox.isChecked)
                .putBoolean(SEARCH_GAME_MEDIUM, binding.mediumGameCheckbox.isChecked)
                .putBoolean(SEARCH_GAME_LARGE, binding.largeGameCheckbox.isChecked)
                .putString(SEARCH_GAME_SPEED, selectedSpeed.toString())
                .apply()
    }

    override fun onValueSelected(e: Entry, h: Highlight) {
        Log.d(TAG, "Val selected: " + chart.getAxisLeft().valueFormatter.getFormattedValue(e.getY()) + ", " + e.getX() + " - " + chart.getData().getDataSetByIndex(h.getDataSetIndex()).getLabel() + " " + e.getData())

    }

    override fun onNothingSelected() {
        Log.d(TAG, "Val unselected")
    }
}

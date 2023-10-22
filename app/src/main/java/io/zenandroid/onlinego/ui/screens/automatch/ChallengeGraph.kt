package io.zenandroid.onlinego.ui.screens.automatch

import android.graphics.Color
import android.util.Log
import android.view.ViewGroup.LayoutParams
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.BubbleChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.LimitLine.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.data.BubbleDataSet
import com.github.mikephil.charting.data.BubbleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.data.model.ogs.SeekGraphChallenge
import io.zenandroid.onlinego.ui.views.ClickableBubbleChart
import io.zenandroid.onlinego.utils.formatRank
import io.zenandroid.onlinego.utils.setMarginsDP
import kotlin.math.abs
import kotlin.math.log10

private const val TAG = "ChallengeGraph"

@Composable
fun ChallengeGraph(
  challenges: List<SeekGraphChallenge>,
  rating: Int,
  onProfile: (String) -> Unit,
  onAccept: (Long) -> Unit,
  modifier: Modifier
) {
  AndroidView(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(ratio = 5f/4f),
    factory = { context ->
      ClickableBubbleChart(context).apply {
        id = R.id.chart
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        setMarginsDP(top = 4)
        description.isEnabled = false
        setOnChartValueSelectedListener(
          object : OnChartValueSelectedListener {
            private val chart: BubbleChart = this@apply

            override fun onValueSelected(e: Entry, h: Highlight) {
              Log.d(TAG, "Val selected: " + chart.axisLeft.valueFormatter.getFormattedValue(e.y) + ", " + e.x + " - " + chart.data.getDataSetByIndex(h.dataSetIndex).label + " " + e.data)
            }

            override fun onNothingSelected() {
              Log.d(TAG, "Val unselected")
            }
          }
        )
        setDrawGridBackground(false)
        setTouchEnabled(true)

        // enable scaling and dragging
        isDragEnabled = true
        setScaleEnabled(true)

        setMaxVisibleValueCount(200)
        setPinchZoom(true)

        // create a dataset and give it a type
        val set1 = BubbleDataSet(ArrayList<BubbleEntry>(), "19x19")
        //set1.setDrawIcons(false)
        set1.setColor(ColorTemplate.COLORFUL_COLORS[0], 130)
        set1.setDrawValues(true)
        set1.isNormalizeSizeEnabled = false

        val set2 = BubbleDataSet(ArrayList<BubbleEntry>(), "13x13")
        //set2.setDrawIcons(false)
        //set2.setIconsOffset(MPPointF(0f, 15f))
        set2.setColor(ColorTemplate.COLORFUL_COLORS[1], 130)
        set2.setDrawValues(true)
        set2.isNormalizeSizeEnabled = false

        val set3 = BubbleDataSet(ArrayList<BubbleEntry>(), "9x9")
        set3.setColor(ColorTemplate.COLORFUL_COLORS[2], 130)
        set3.setDrawValues(true)
        set3.isNormalizeSizeEnabled = false

        val set4 = BubbleDataSet(ArrayList<BubbleEntry>(), "?x?")
        set4.setColor(ColorTemplate.COLORFUL_COLORS[3], 130)
        set4.setDrawValues(true)
        set4.isNormalizeSizeEnabled = false

        val set5 = BubbleDataSet(ArrayList<BubbleEntry>(), "Eligible")
        set5.setDrawIcons(false)
        set5.setColor(Color.BLUE, 130)
        set5.setDrawValues(true)
        set5.isNormalizeSizeEnabled = false

        val set6 = BubbleDataSet(ArrayList<BubbleEntry>(), "Rengo")
        set6.setColor(Color.GRAY, 130)
        set6.setDrawValues(false)
        set6.isNormalizeSizeEnabled = false

        val dataSets = ArrayList<IBubbleDataSet>()
        dataSets.add(set1) // add the data sets
        dataSets.add(set2)
        dataSets.add(set3)
        dataSets.add(set4)
        dataSets.add(set5)
        dataSets.add(set6)

        // create a data object with the data sets
        val data = BubbleData(dataSets)
        data.setDrawValues(false)
        data.setValueTextSize(8f)
        data.setValueTextColor(Color.WHITE)
        data.setHighlightCircleWidth(1.5f)

        this.data = data
        this.invalidate()

        legend.apply {
          verticalAlignment = Legend.LegendVerticalAlignment.TOP
          horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
          orientation = Legend.LegendOrientation.VERTICAL
          setDrawInside(false)
          textColor = ResourcesCompat.getColor(resources, R.color.colorText, context.theme)
        }

        axisLeft.apply {
          spaceTop = 30f
          spaceBottom = 30f
          setDrawZeroLine(false)
          setLabelCount(10, true)
          setAxisMinValue(-1f)
          setAxisMaxValue(38f)
          isGranularityEnabled = true
          granularity = 1f
          valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
              return formatRank(value.toDouble())
            }
          }
          textColor = ResourcesCompat.getColor(resources, R.color.colorText, context.theme)
          rating.toFloat().let {
            addLimitLine(LimitLine(it, "").apply {
              lineWidth = .5f
              lineColor = Color.WHITE
              labelPosition = LimitLabelPosition.RIGHT_TOP
              textSize = 10f
            })
          }
        }

        axisRight.isEnabled = false

        xAxis.apply {
          position = XAxis.XAxisPosition.BOTTOM
          valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = when(value.toInt()) {
              0 -> "Blitz"
              3 -> "Live"
              6 -> "Correspondence"
              else -> ""
            }
          }
          //setLabelCount(4, true)
          setCenterAxisLabels(true)
          labelRotationAngle = 9f
          setAxisMinValue(0f)
          setAxisMaxValue(8f)
          isGranularityEnabled = true
          granularity = 1f
          textColor = ResourcesCompat.getColor(resources, R.color.colorText, context.theme)
          addLimitLine(LimitLine(2f, "").apply {
            lineWidth = 1.5f
            lineColor = Color.GRAY
            labelPosition = LimitLabelPosition.RIGHT_TOP
            textSize = 10f
          })
          addLimitLine(LimitLine(5f, "").apply {
            lineWidth = 1.5f
            lineColor = Color.GRAY
            labelPosition = LimitLabelPosition.RIGHT_TOP
            textSize = 10f
          })
        }

        setNoDataTextColor(ResourcesCompat.getColor(resources, R.color.colorActionableText, context.theme))

        let { chart ->
          // create a custom MarkerView (extend MarkerView) and specify the layout to use for it
          val mv = ChallengeMarkerView(context, rating,
            { onProfile(it) },
            { onAccept(it) })
          mv.chartView = chart
          chart.marker = mv
        }
      }
    },
    update = { chart ->
      (chart as BubbleChart).apply {
        for(i in 0..5)
          data.getDataSetByIndex(i).clear()

        data.also {
          challenges.forEach { challenge: SeekGraphChallenge ->
            val rankDiff = (challenge.rank ?: 0.0) - rating.toDouble()
            val eligible = when {
              challenge.ranked && abs(rankDiff) > 9 -> false
              rating < challenge.min_rank -> false
              rating > challenge.max_rank -> false
              challenge.rengo -> false
              else -> true
            }
            val drawable = if (eligible) resources.getDrawable(R.drawable.ic_star) else null
            val dataset = when {
              challenge.rengo -> 5
              eligible -> 4
              challenge.width == 19 -> 0
              challenge.width == 13 -> 1
              challenge.width == 9 -> 2
              else -> 3
            }
            val entry = BubbleEntry(
              log10((challenge.time_per_move ?: 0.0) + 1).toFloat(),
              challenge.rank?.toFloat() ?: 0f,
              .2f, drawable, challenge)
            data.addEntry(entry, dataset)
          }

          data.notifyDataChanged()
        }

        notifyDataSetChanged()
        invalidate()
      }
    }
  )
}

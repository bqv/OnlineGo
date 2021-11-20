package io.zenandroid.onlinego.ui.composables

import android.graphics.Paint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.ui.theme.OnlineGoTheme

/*
@Composable
fun Shimmer(modifier: Modifier) {
	val paint = remember { Paint().apply {
		isAntiAlias = true
        style = PaintingStyle.fill
        color = "#efefef".color
	} }
	val t = animatedFloat(0f)
    onActive {
        t.loop(from = 0f, to = 1f, duration = 1000)
        onDispose {
            t.stop()
        }
    }
	Canvas(modifier) {
		paint.shader = LinearGradientShader(
			size.topLeft,
			size.bottomRight,
			shaderColors,
			listOf(0f, t.value, 1f)
		)
		drawRect(
			rect = size, 
			paint = paint
		)
	}
}

fun Modifier.shimmer() = composed {
	val progress = animatedFloat(0f)
	onActive {
		progress.loop(0f, 1f, 1000)
		onDispose {
			progress.stop()
		}
	}
	remember { ShimmerModifier(progress) }
}

private class ShimmerModifier(val t: AnimatedFloat) : DrawModifier {
	private val shaderColors = listOf(
		"#AAAAAA".color, 
		"#a2AAAAAA".color,
		"#AAAAAA".color
	)
	private val paint = Paint().apply {
		isAntiAlias = true
		style = PaintingStyle.fill
		color = "#efefef".color
	}
	override fun ContentDrawScope.draw() {
		paint.shader = LinearGradientShader(
			size.topLeft,
			size.bottomRight,
			shaderColors,
			listOf(0f, t.value, 1f)
		)
		drawRect(
			rect = size, 
			paint = paint
		)
	}
}

@Composable
fun ShimmerBox(modifier: Modifier) {
	Box(modifier.shimmer())
}

private val String.color: Color 
	get() = Color(android.graphics.Color.parseColor(value))

private fun AnimatedFloat.loop(from: Flaot, to: Float, durationMs: Int) {
	fun singleLoop() {
		snapTo(from)
        animateTo(
            to,
            TweenBuilder<Float>.apply { duration = durationMs },
            onEnd = { reason, _ ->
                if (reason == AnimationEndReason.TargetReached) singleLoop()
            }
        )
	}
	singleLoop()
}
*/

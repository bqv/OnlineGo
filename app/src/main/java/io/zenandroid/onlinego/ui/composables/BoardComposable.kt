package io.zenandroid.onlinego.ui.composables

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.ColorInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withSave
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.data.model.Position
import io.zenandroid.onlinego.data.model.StoneType
import io.zenandroid.onlinego.data.model.ogs.PlayCategory
import kotlinx.coroutines.coroutineScope
import kotlin.math.ceil
import kotlin.math.roundToInt


private val whiteStone = VectorDrawableCompat.create(OnlineGoApplication.instance.resources, R.drawable.ic_stone_white_svg, null)!!
private val blackStone = VectorDrawableCompat.create(OnlineGoApplication.instance.resources, R.drawable.ic_stone_black_svg, null)!!
private val shadowDrawable = ResourcesCompat.getDrawable(OnlineGoApplication.instance.resources, R.drawable.gradient_shadow, null)!!

@ExperimentalComposeUiApi
@Composable
fun Board(
        modifier: Modifier = Modifier,
        boardWidth: Int,
        boardHeight: Int,
        position: Position?,
        candidateMove: Point? = null,
        candidateMoveType: StoneType? = null,
        drawCoordinates: Boolean = true,
        interactive: Boolean = true,
        drawShadow: Boolean = true,
        fadeInLastMove: Boolean = true,
        fadeOutRemovedStones: Boolean = true,
        removedStones: Map<Point, StoneType>? = null,
        onTapMove: ((Point) -> Unit)? = null,
        onTapUp: ((Point) -> Unit)? = null
) {
    val drawLastMove = true
    val drawMarks = true
    val background: ImageBitmap = ImageBitmap.imageResource(id = R.mipmap.texture)

    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    val measurements = remember(width, height, boardWidth, boardHeight) { doMeasurements(width, height, boardWidth, boardHeight, drawCoordinates) }

    var lastHotTrackedPoint: Point? by remember { mutableStateOf(null) }
    var stoneToFadeIn: Point? by remember(position?.lastMove) { mutableStateOf(position?.lastMove) }
    var stonesToFadeOut: Map<Point, StoneType>? by remember(removedStones) { mutableStateOf(removedStones) }

    val fadeInAlpha = remember { Animatable(.4f) }
    val fadeOutAlpha = remember { Animatable(1f) }

    if(fadeInLastMove && stoneToFadeIn != null) {
        LaunchedEffect(stoneToFadeIn) {
            fadeInAlpha.snapTo(.4f)
            fadeInAlpha.animateTo(1f, animationSpec = tween(150))
            stoneToFadeIn = null
        }
    }
    if(fadeOutRemovedStones && stonesToFadeOut?.isNotEmpty() == true) {
        LaunchedEffect(stonesToFadeOut) {
            fadeOutAlpha.snapTo(1f)
            fadeOutAlpha.animateTo(0f, animationSpec = tween(150, 75))
            stonesToFadeOut = null
        }
    }
    Canvas(modifier = modifier
            .aspectRatio(1f)
            .run {
                if(interactive) {
                    pointerInteropFilter {
                        if(measurements.cellSize == 0) {
                            return@pointerInteropFilter false
                        }
                        val eventCoords = screenToBoardCoordinates(it.x, it.y, measurements, boardWidth, boardHeight)

                        if (eventCoords != lastHotTrackedPoint) {
                            onTapMove?.invoke(eventCoords)
                            lastHotTrackedPoint = eventCoords
                        }

                        if (it.action == MotionEvent.ACTION_UP) {
                            onTapUp?.invoke(eventCoords)
                            lastHotTrackedPoint = null
                        }
                        true
                    }
                } else {
                    this
                }
            }
            .onGloballyPositioned {
                width = it.size.width
                height = it.size.height
            }
    ) {
        if (measurements.width == 0) {
            return@Canvas
        }
        arrayOf(whiteStone, blackStone).forEach {
            it.setBounds(-measurements.stoneRadius, -measurements.stoneRadius, measurements.stoneRadius, measurements.stoneRadius)
        }

        drawImage(
                image = background,
                dstSize = IntSize(ceil(this.size.width).toInt(), ceil(this.size.height).toInt())
        )
        translate(measurements.border + measurements.xOffsetForNonSquareBoard, measurements.border + measurements.yOffsetForNonSquareBoard) {

            drawGrid(boardWidth, boardHeight, candidateMove, measurements)
            drawStarPoints(boardWidth, boardHeight, measurements)
            drawCoordinates(boardWidth, boardHeight, drawCoordinates, measurements)

            for (item in stonesToFadeOut ?: emptyMap()) {
                drawStone(item.key, item.value, fadeOutAlpha.value, true, measurements)
            }
            position?.let {
                // stones
                for (p in position.allStonesCoordinates) {
                    val type = position.getStoneAt(p.x, p.y)
                    val alpha = when {
                        fadeInLastMove && p == stoneToFadeIn -> fadeInAlpha.value
                        fadeOutRemovedStones && it.removedSpots.contains(p) -> .4f
                        else -> 1f
                    }
                    drawStone(p, type, alpha, drawShadow, measurements)
                }

                drawDecorations(it, drawLastMove, drawMarks, measurements)

//                drawTerritory(canvas, it)
//                drawAiEstimatedOwnership(canvas, it)
//                drawHints(canvas, it)
            }
            candidateMove?.let {
                drawStone(it, candidateMoveType, .4f, false, measurements)
            }
        }
    }
}

private fun screenToBoardCoordinates(x: Float, y: Float, measurements: Measurements, boardWidth: Int, boardHeight: Int): Point {
    return Point(
            ((x - measurements.border - measurements.xOffsetForNonSquareBoard).toInt() / measurements.cellSize).coerceIn(0, boardWidth - 1),
            ((y - measurements.border - measurements.yOffsetForNonSquareBoard).toInt() / measurements.cellSize).coerceIn(0, boardHeight - 1)
    )
}

private val coordinatesX = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
private val coordinatesY = (1..25).map(Int::toString)

private data class Measurements(
        var width: Int,
        val cellSize: Int,
        val border: Float,
        val linesWidth: Float,
        val highlightLinesWidth: Float,
        val decorationsLineWidth: Float,
        val stoneSpacing: Float,
        val textSize: Float,
        val coordinatesTextSize: Float,
        val shadowPaint: Paint,
        val halfCell: Float,
        val stoneRadius: Int,
        val xOffsetForNonSquareBoard: Float,
        val yOffsetForNonSquareBoard: Float,
        val whiteStoneBitmap: ImageBitmap?,
        val blackStoneBitmap: ImageBitmap?
)

private fun getCellCenter(i: Int, j: Int, measurements: Measurements) =
        PointF(i * measurements.cellSize + measurements.halfCell, j * measurements.cellSize + measurements.halfCell)

private fun DrawScope.drawSingleStarPoint(i: Int, j: Int, measurements: Measurements) {
    val center = getCellCenter(i, j, measurements)
    drawCircle(Color.Black, measurements.cellSize / 15f, Offset(center.x, center.y))
}

private fun DrawScope.drawTextCentred(text: String, cx: Float, cy: Float, textSize: Float, @ColorInt color: Int = android.graphics.Color.BLACK, shadow: Boolean = true, ignoreAscentDescent: Boolean = false) {
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    val textBounds = Rect()
    paint.apply {
        textAlign = android.graphics.Paint.Align.LEFT
        isSubpixelText = true
        setTextSize(textSize)
        getTextBounds(text, 0, text.length, textBounds)
        setColor(color)
        if(shadow) {
            setShadowLayer(8.dp.value, 0f, 0f, android.graphics.Color.WHITE)
        }
    }

    drawIntoCanvas {
        it.nativeCanvas.drawText(text,
                cx - textBounds.exactCenterX(),
                cy - textBounds.exactCenterY() + if (ignoreAscentDescent) (textBounds.bottom.toFloat() / 2) else 0f,
                paint)
    }
}

private fun DrawScope.drawDecorations(position: Position, drawLastMove: Boolean, drawMarks: Boolean, measurements: Measurements) {
    if(drawLastMove) {
        position.lastMove?.let {
            if(it.x != -1) {
                val center = getCellCenter(it.x, it.y, measurements)
                val color = if (position.lastPlayerToMove == StoneType.WHITE) Color.Black else Color.White

                drawCircle(color, measurements.cellSize / 4f, Offset(center.x, center.y), style = Stroke(measurements.decorationsLineWidth))
            }
        }
    }

    position.variation.forEachIndexed { i, p ->
        val center = getCellCenter(p.x, p.y, measurements)
        val stone = position.getStoneAt(p)
        val color = if(stone == null || stone == StoneType.WHITE) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        drawTextCentred((i + 1).toString(), center.x, center.y, textSize = measurements.textSize, color)
    }

    if(drawMarks) {
        position.customMarks.forEach {
            when(it.text) {
                "#" -> { // last move mark
                    val type = position.getStoneAt(it.placement)
                    val center = getCellCenter(it.placement.x, it.placement.y, measurements)

                    val color = if (type == StoneType.WHITE) Color.Black else Color.White

                    drawCircle(color, measurements.cellSize / 4f, Offset(center.x, center.y), style = Stroke(measurements.decorationsLineWidth))
                }
                else -> {
                    val center = getCellCenter(it.placement.x, it.placement.y, measurements)

                    val color = when(it.category) {
                        PlayCategory.IDEAL -> Color(0xC0D0FFC0)
                        PlayCategory.GOOD -> Color(0xC0F0FF90)
                        PlayCategory.MISTAKE -> Color(0xC0ED7861)
                        PlayCategory.TRICK -> Color(0xC0D384F9)
                        PlayCategory.QUESTION -> Color(0xC079B3E4)
                        PlayCategory.LABEL -> Color(0x70FFFFFF)
                        null -> Color(0x00FFFFFF)
                    }


                    drawCircle(color, measurements.cellSize / 2f - measurements.stoneSpacing, Offset(center.x, center.y))
                    it.text?.let {
                        drawTextCentred(it, center.x, center.y, textSize = measurements.textSize)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStone(p: Point, type: StoneType?, alpha: Float = 1f, drawShadow: Boolean, measurements: Measurements) {
    val center = getCellCenter(p.x, p.y, measurements)
    drawIntoCanvas {
        if(drawShadow && alpha > .75) {
            if(Build.VERSION.SDK_INT >= 28) {
                it.drawCircle(Offset(center.x, center.y), measurements.stoneRadius - 1.5f, measurements.shadowPaint) // we don't care about the circle really, but the paint produces a fast shadow...
            } else {
                shadowDrawable.setBounds(
                        (center.x - measurements.stoneRadius - measurements.cellSize / 20f).toInt(),
                        (center.y - measurements.stoneRadius - measurements.cellSize / 20f).toInt(),
                        (center.x + measurements.stoneRadius + measurements.cellSize / 12f).toInt(),
                        (center.y + measurements.stoneRadius + measurements.cellSize / 9f).toInt()
                )
                shadowDrawable.draw(it.nativeCanvas)
            }
        }

        if(measurements.cellSize > 30) {
            if (alpha != 1f) {
                val bitmap =
                    if (type == StoneType.WHITE) measurements.whiteStoneBitmap else measurements.blackStoneBitmap
                drawImage(
                    bitmap!!,
                    Offset(
                        center.x - measurements.stoneRadius,
                        center.y - measurements.stoneRadius
                    ),
                    alpha = alpha
                )
            } else {
                val stone = if (type == StoneType.WHITE) whiteStone else blackStone
                it.nativeCanvas.withSave {
                    it.nativeCanvas.translate(center.x, center.y)
                    stone.draw(it.nativeCanvas)
                }
            }
        } else {
            val color = if (type == StoneType.BLACK) Color.Black else Color.White

//            stonePaint.style = android.graphics.Paint.Style.FILL
            drawCircle(color = color, center = Offset(center.x, center.y), radius = measurements.cellSize / 2f - measurements.stoneSpacing, style = Fill)
            if(type == StoneType.WHITE) {
//                stonePaint.color = 0xCC331810.toInt()
//                stonePaint.style = android.graphics.Paint.Style.STROKE
                drawCircle(color = Color(0xCC331810), center = Offset(center.x, center.y), radius = measurements.cellSize / 2f - measurements.stoneSpacing, style = Stroke(1f))
            }

        }
    }
}

private fun DrawScope.drawGrid(boardWidth: Int, boardHeight: Int, candidateMove: Point?, measurements: Measurements) {
    for (i in 0 until boardWidth) {
        val start = i * measurements.cellSize + measurements.halfCell
        val fullLength = (measurements.cellSize * boardHeight).toFloat()

        val (color, lineWidth) = if(i == candidateMove?.x) Color.White to measurements.highlightLinesWidth else Color.Black to measurements.linesWidth
        drawLine(color, Offset(start, measurements.halfCell), Offset(start, fullLength - measurements.halfCell), lineWidth)
    }
    for (i in 0 until boardHeight) {
        val start = i * measurements.cellSize + measurements.halfCell
        val fullLength = (measurements.cellSize * boardWidth).toFloat()

        val (color, lineWidth) = if(i == candidateMove?.x) Color.White to measurements.highlightLinesWidth else Color.Black to measurements.linesWidth
        drawLine(color, Offset(measurements.halfCell, start), Offset(fullLength - measurements.halfCell, start), lineWidth)
    }
}

private fun DrawScope.drawStarPoints(boardWidth: Int, boardHeight: Int, measurements: Measurements) {
    if(boardWidth == boardHeight) {
        when (boardWidth) {
            19 -> {
                drawSingleStarPoint(3, 3, measurements)
                drawSingleStarPoint(15, 15, measurements)
                drawSingleStarPoint(3, 15, measurements)
                drawSingleStarPoint(15, 3, measurements)

                drawSingleStarPoint(3, 9, measurements)
                drawSingleStarPoint(9, 3, measurements)
                drawSingleStarPoint(15, 9, measurements)
                drawSingleStarPoint(9, 15, measurements)

                drawSingleStarPoint(9, 9, measurements)
            }
            13 -> {
                drawSingleStarPoint(3, 3, measurements)
                drawSingleStarPoint(9, 9, measurements)
                drawSingleStarPoint(3, 9, measurements)
                drawSingleStarPoint(9, 3, measurements)

                drawSingleStarPoint(6, 6, measurements)
            }
            9 -> {
                drawSingleStarPoint(2, 2, measurements)
                drawSingleStarPoint(6, 6, measurements)
                drawSingleStarPoint(2, 6, measurements)
                drawSingleStarPoint(6, 2, measurements)

                drawSingleStarPoint(4, 4, measurements)
            }
        }
    }
}

private fun DrawScope.drawCoordinates(boardWidth: Int, boardHeight: Int, drawCoordinates: Boolean, measurements: Measurements) {
    if (drawCoordinates) {
        for (i in 0 until boardWidth) {
            drawTextCentred(coordinatesX[i], getCellCenter(i, 0, measurements).x, 0f - measurements.border / 2, textSize = measurements.coordinatesTextSize, shadow = false, ignoreAscentDescent = true)
            drawTextCentred(coordinatesX[i], getCellCenter(i, 0, measurements).x, measurements.cellSize * boardHeight + measurements.border / 2, textSize = measurements.coordinatesTextSize, shadow = false, ignoreAscentDescent = true)
        }
        for (i in boardHeight downTo 1) {
            drawTextCentred(coordinatesY[i - 1], 0f - measurements.border / 2, getCellCenter(0, boardHeight - i, measurements).y, textSize = measurements.coordinatesTextSize, shadow = false)
            drawTextCentred(coordinatesY[i - 1], measurements.cellSize * boardWidth + measurements.border / 2, getCellCenter(0, boardHeight - i, measurements).y, textSize = measurements.coordinatesTextSize, shadow = false)
        }
    }

}

private fun doMeasurements(width: Int, height: Int, boardWidth: Int, boardHeight: Int, drawCoordinates: Boolean): Measurements {
    val usableWidth = if (drawCoordinates) {
        width - (width  / boardWidth.toFloat()).roundToInt()
    } else width

    val usableHeight = if (drawCoordinates) {
        height - (height  / boardHeight.toFloat()).roundToInt()
    } else height

    val cellSize = minOf(usableWidth / boardWidth, usableHeight / boardHeight)
    val border = minOf((width - boardWidth * cellSize) / 2f, (width - boardHeight * cellSize) / 2f)
    val xOffsetForNonSquareBoard = ((boardHeight - boardWidth) * cellSize / 2f).coerceAtLeast(0f)
    val yOffsetForNonSquareBoard = ((boardWidth - boardHeight) * cellSize / 2f).coerceAtLeast(0f)

    val linesWidth = (cellSize / 35f).coerceAtMost(2f)
    val highlightLinesWidth = linesWidth * 2
    val decorationsLineWidth = cellSize / 20f
    val stoneSpacing = (cellSize / 35f).coerceAtLeast(1f)
    val textSize = cellSize.toFloat() * .65f
    val coordinatesTextSize = cellSize.toFloat() * .4f
    val shadowPaint = Paint().apply {
            asFrameworkPaint().run {
                color = 0x01FF0000
                setShadowLayer(
                        cellSize / 10f,
                        0f,
                        stoneSpacing * 2,
                        0xBB000000.toInt()
                )
            }
        }
    val halfCell = cellSize / 2f
    val stoneRadius = (cellSize / 2f - stoneSpacing).toInt()

    val whiteBitmap = if(width <= 0) null else {
        convertVectorIntoBitmap(whiteStone, ceil(cellSize - 2 * stoneSpacing).toInt())
    }

    val blackBitmap = if(width <= 0) null else {
        convertVectorIntoBitmap(blackStone, ceil(cellSize - 2 * stoneSpacing).toInt())
    }

    return Measurements(
            cellSize = cellSize,
            border = border,
            linesWidth = linesWidth,
            highlightLinesWidth = highlightLinesWidth,
            decorationsLineWidth = decorationsLineWidth,
            stoneSpacing = stoneSpacing,
            width = width,
            textSize = textSize,
            coordinatesTextSize = coordinatesTextSize,
            shadowPaint = shadowPaint,
            halfCell = halfCell,
            stoneRadius = stoneRadius,
            xOffsetForNonSquareBoard = xOffsetForNonSquareBoard,
            yOffsetForNonSquareBoard = yOffsetForNonSquareBoard,
            blackStoneBitmap = blackBitmap,
            whiteStoneBitmap = whiteBitmap
    )
}

private fun convertVectorIntoBitmap(vector: VectorDrawableCompat, width: Int): ImageBitmap {
    val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), width.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    vector.setBounds(0, 0, canvas.width, canvas.height)
    vector.draw(canvas)
    return bitmap.asImageBitmap()
}

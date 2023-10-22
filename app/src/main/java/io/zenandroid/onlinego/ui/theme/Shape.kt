package io.zenandroid.onlinego.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.material3.Shapes as Shapes3
import androidx.compose.ui.unit.dp

val shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(5.dp),
    large = RoundedCornerShape(10.dp)
)

val shapes3 = Shapes3(
    small = shapes.small,
    medium = shapes.medium,
    large = shapes.large
)

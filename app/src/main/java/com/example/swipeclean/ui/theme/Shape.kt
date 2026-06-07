package com.example.swipeclean.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Expressive Geometry based on Tonal Clarity Guidelines
val SwipeCleanShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp), // sm: 0.25rem
    small = RoundedCornerShape(8.dp),      // DEFAULT: 0.5rem
    medium = RoundedCornerShape(12.dp),    // md: 0.75rem
    large = RoundedCornerShape(16.dp),     // lg: 1rem
    extraLarge = RoundedCornerShape(24.dp) // xl: 1.5rem
)

val ShapeFull = CircleShape // full: 9999px

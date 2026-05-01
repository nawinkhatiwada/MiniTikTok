package com.androidbolts.minitiktok.features.create.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.util.VelocityTrackerAddPointsFix
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun CaptureButton(
    isRecording: Boolean,
    onTap: () -> Unit,
    onRecordStart: () -> Unit,
    onRecordEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sizes derived from screen width so the button scales across all devices
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val buttonSize  = screenWidth * 0.22f
    val innerSize   = screenWidth * 0.17f
    val ringStroke  = screenWidth * 0.015f

    // Keep latest lambdas without restarting gesture detection
    val latestOnTap         by rememberUpdatedState(onTap)
    val latestOnRecordStart by rememberUpdatedState(onRecordStart)
    val latestOnRecordEnd   by rememberUpdatedState(onRecordEnd)

    // Ring progress animates over 60 s while recording
    val ringProgress = remember { Animatable(0f) }
    LaunchedEffect(isRecording) {
        if (isRecording) {
            ringProgress.snapTo(0f)
            ringProgress.animateTo(1f, tween(60_000, easing = LinearEasing))
        } else {
            ringProgress.snapTo(0f)
        }
    }

    // Inner circle shrinks when recording starts
    val innerScale by animateFloatAsState(
        targetValue    = if (isRecording) 0.60f else 1f,
        animationSpec  = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label          = "inner_scale"
    )
    // Ring fades in/out
    val ringAlpha by animateFloatAsState(
        targetValue   = if (isRecording) 1f else 0f,
        animationSpec = tween(220),
        label         = "ring_alpha"
    )

    val progress = ringProgress.value

    Box(
        modifier = modifier
            .requiredSize(buttonSize)   // ignores parent height constraints → always a circle
            .pointerInput(Unit) {
               awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    val longPressMs = viewConfiguration.longPressTimeoutMillis
                    val up = withTimeoutOrNull(longPressMs) {
                        waitForUpOrCancellation()
                    }
                    if (up == null) {
                        latestOnRecordStart()
                        waitForUpOrCancellation()
                        latestOnRecordEnd()
                    } else {
                        latestOnTap()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Animated ring drawn on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = ringStroke.toPx()
            val radius   = size.minDimension / 2f - strokePx / 2f
            // inset the arc bounding box by strokePx/2 so it sits on the same
            // circle as drawCircle (which centers the stroke on the radius)
            val arcInset = strokePx / 2f
            val arcSize  = Size(size.width - strokePx, size.height - strokePx)
            val arcOffset = Offset(arcInset, arcInset)

            // Faint ring background
            drawCircle(
                color  = Color.White.copy(alpha = 0.28f * ringAlpha),
                radius = radius,
                style  = Stroke(strokePx)
            )
            // TikTok-red progress arc — same bounding circle as the ring above
            if (ringAlpha > 0f && progress > 0f) {
                drawArc(
                    color      = Color(0xFFFF2D55),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter  = false,
                    topLeft    = arcOffset,
                    size       = arcSize,
                    style      = Stroke(width = strokePx, cap = StrokeCap.Round),
                    alpha      = ringAlpha
                )
            }
        }

        // Inner white circle — scales down when recording
        Box(
            modifier = Modifier
                .size(innerSize)
                .scale(innerScale)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

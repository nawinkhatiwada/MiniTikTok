package com.androidbolts.minitiktok.features.edit.presentation

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import com.androidbolts.minitiktok.R
import com.androidbolts.minitiktok.features.create.presentation.VideoPreview
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Sheet expands to this fraction of screen height.
private const val EXPANDED_SHEET_FRACTION = 0.40f

// ── Tool descriptors ──────────────────────────────────────────────────────────

private enum class ToolIconType { TEXT, DRAWABLE }

private data class EditTool(val abbr: String, val label: String, val drawableRes: Int? = null) {
    val iconType get() = if (drawableRes != null) ToolIconType.DRAWABLE else ToolIconType.TEXT
}

private val editTools = listOf(
    EditTool("Tr", "Trim"),
    EditTool("",   "Music",     R.drawable.ic_music_note),
    EditTool("T",  "Text"),
    EditTool("F",  "Filters"),
    EditTool("Ef", "Effects"),
    EditTool("St", "Stickers"),
    EditTool("Cc", "Captions"),
    EditTool("",   "Voiceover", R.drawable.ic_mic),
    EditTool("Sp", "Speed"),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun EditScreen(
    uri: Uri,
    isFrontCamera: Boolean = false,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isVideo = remember(uri) {
        context.contentResolver.getType(uri)?.startsWith("video/") == true
                || uri.lastPathSegment?.endsWith(".mp4", ignoreCase = true) == true
    }

    val density = LocalDensity.current
    val scope   = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // peekHeight fills the gap below the 9:16 video so the sheet top aligns
        // exactly with the video bottom. expandedHeight = 40% of the screen.
        val videoHeight: Dp    = maxWidth * (16f / 9f)
        val peekHeight: Dp     = (maxHeight - videoHeight).coerceAtLeast(80.dp)
        val expandedHeight: Dp = maxHeight * EXPANDED_SHEET_FRACTION

        val collapsedOffsetPx = with(density) { (maxHeight - peekHeight).toPx() }
        val expandedOffsetPx  = with(density) { (maxHeight - expandedHeight).toPx() }
        val scalingRangePx    = collapsedOffsetPx - expandedOffsetPx

        // Sheet Y offset — drives everything. 0px = top of screen (impossible here);
        // collapsedOffsetPx = peeking; expandedOffsetPx = fully open.
        val sheetOffset = remember { Animatable(0f) }

        // Initialise / update bounds whenever layout metrics change.
        LaunchedEffect(collapsedOffsetPx, expandedOffsetPx) {
            sheetOffset.updateBounds(expandedOffsetPx, collapsedOffsetPx)
            sheetOffset.snapTo(collapsedOffsetPx)
        }

        // 0f = collapsed  →  1f = fully expanded
        val sheetProgress by remember {
            derivedStateOf {
                if (scalingRangePx == 0f) 0f
                else ((collapsedOffsetPx - sheetOffset.value) / scalingRangePx).coerceIn(0f, 1f)
            }
        }

        // Shared drag state — used by both the video area and the sheet strip so
        // dragging anywhere moves the sheet smoothly in real-time.
        val draggableState = rememberDraggableState { delta ->
            scope.launch {
                sheetOffset.snapTo(
                    (sheetOffset.value + delta).coerceIn(expandedOffsetPx, collapsedOffsetPx)
                )
            }
        }

        fun settle(velocityPx: Float) {
            scope.launch {
                val midpoint = (collapsedOffsetPx + expandedOffsetPx) / 2f
                val target = when {
                    velocityPx < -500f -> expandedOffsetPx
                    velocityPx >  500f -> collapsedOffsetPx
                    sheetOffset.value < midpoint -> expandedOffsetPx
                    else -> collapsedOffsetPx
                }
                sheetOffset.animateTo(
                    target,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMedium
                    )
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

            // ── Video body — padded at bottom by peek height ──────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = peekHeight)
                    .draggable(
                        state          = draggableState,
                        orientation    = Orientation.Vertical,
                        onDragStopped  = { velocityPx -> settle(velocityPx) }
                    )
            ) {
                VideoLayer(
                    uri           = uri,
                    isVideo       = isVideo,
                    isFrontCamera = isFrontCamera,
                    sheetProgress = sheetProgress,
                    onBack        = onBack
                )
            }

            // ── Sheet — slides up/down via offset ─────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, sheetOffset.value.roundToInt()) }
                    .background(Color(0xFF111111))
                    .draggable(
                        state         = draggableState,
                        orientation   = Orientation.Vertical,
                        onDragStopped = { velocityPx -> settle(velocityPx) }
                    )
            ) {
                EditSheetContent(minHeight = expandedHeight)
            }
        }
    }
}

// ── Video layer ───────────────────────────────────────────────────────────────

@Composable
private fun VideoLayer(
    uri: Uri,
    isVideo: Boolean,
    isFrontCamera: Boolean,
    sheetProgress: Float,
    onBack: () -> Unit
) {
    Box(
        modifier         = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {
        // 9:16 video — scales uniformly from center, corners round as sheet rises
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .graphicsLayer {
                    val s        = lerp(1f, 0.78f, sheetProgress)
                    scaleX          = s
                    scaleY          = s
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    val cornerPx    = lerp(0f, 22.dp.toPx(), sheetProgress)
                    clip            = cornerPx > 0f
                    shape           = RoundedCornerShape(cornerPx)
                }
        ) {
            if (isVideo) {
                VideoPreview(
                    modifier      = Modifier.fillMaxSize(),
                    uri           = uri,
                    isFrontCamera = isFrontCamera
                )
            } else {
                AsyncImage(
                    model              = uri,
                    contentDescription = "Captured photo",
                    modifier           = Modifier
                        .fillMaxSize()
                        .graphicsLayer { scaleX = if (isFrontCamera) -1f else 1f },
                    contentScale       = ContentScale.Fit
                )
            }
        }

        // Close — top left
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.50f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onBack
                )
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.Close,
                contentDescription = "Back",
                tint               = Color.White,
                modifier           = Modifier.size(18.dp)
            )
        }

        // Next — top right
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFEE1D52))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null
                ) { }
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .align(Alignment.TopEnd)
        ) {
            Text(
                text       = "Next",
                color      = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp
            )
        }
    }
}

// ── Bottom sheet content ──────────────────────────────────────────────────────

@Composable
private fun EditSheetContent(minHeight: Dp = 200.dp) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
    ) {
        // Subtle separator between video and sheet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.12f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            editTools.forEach { tool -> EditToolButton(tool) }
        }
    }
}

// ── Tool button ───────────────────────────────────────────────────────────────

@Composable
private fun EditToolButton(tool: EditTool) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null
                ) { },
            contentAlignment = Alignment.Center
        ) {
            when (tool.iconType) {
                ToolIconType.DRAWABLE -> Icon(
                    painter            = painterResource(tool.drawableRes!!),
                    contentDescription = tool.label,
                    tint               = Color.White,
                    modifier           = Modifier.size(20.dp)
                )
                ToolIconType.TEXT -> Text(
                    text       = tool.abbr,
                    color      = Color.White,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text     = tool.label,
            color    = Color.White.copy(alpha = 0.80f),
            fontSize = 10.sp
        )
    }
}

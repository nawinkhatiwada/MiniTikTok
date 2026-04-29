package com.androidbolts.minitiktok.features.edit.presentation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import coil3.compose.AsyncImage
import com.androidbolts.minitiktok.R
import com.androidbolts.minitiktok.features.create.presentation.VideoPreview

// Video scales from 1.0 → 0.6 over this many dp of sheet drag.
private val SCALING_RANGE_DP = 280.dp

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

@OptIn(ExperimentalMaterial3Api::class)
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

    val sheetState   = rememberStandardBottomSheetState(
        initialValue    = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Compute peek height so the sheet top aligns exactly with the video bottom.
        // video height = screenWidth × (16/9).  peekHeight = screenHeight - videoHeight.
        // Clamp to at least 80dp on very wide screens.
        val videoHeight: Dp = maxWidth * (16f / 9f)
        val peekHeight: Dp  = (maxHeight - videoHeight).coerceAtLeast(80.dp)

        val screenHeightPx  = constraints.maxHeight.toFloat()
        val peekPx          = with(density) { peekHeight.toPx() }
        val scalingRangePx  = with(density) { SCALING_RANGE_DP.toPx() }

        // 0f = sheet at peek  →  1f = video fully scaled
        val sheetProgress by remember(screenHeightPx, peekPx, scalingRangePx) {
            derivedStateOf {
                val collapsedOffset = screenHeightPx - peekPx
                val offset = try {
                    sheetState.requireOffset()
                } catch (_: Throwable) {
                    collapsedOffset
                }
                val draggedUp = (collapsedOffset - offset).coerceAtLeast(0f)
                (draggedUp / scalingRangePx).coerceIn(0f, 1f)
            }
        }

        BottomSheetScaffold(
            scaffoldState        = scaffoldState,
            sheetPeekHeight      = peekHeight,
            containerColor       = Color.Black,
            sheetContainerColor  = Color(0xFF111111),
            sheetTonalElevation  = 0.dp,
            sheetShadowElevation = 0.dp,
            sheetShape           = RectangleShape,
            sheetDragHandle      = null,
            sheetContent         = { EditSheetContent() }
        ) { innerPadding ->
            // Detect vertical swipes on the video body and snap the sheet to the
            // nearest anchor — expands on swipe-up, collapses on swipe-down.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pointerInput(sheetState) {
                        var netDelta = 0f
                        detectVerticalDragGestures(
                            onDragStart  = { netDelta = 0f },
                            onDragCancel = { netDelta = 0f },
                            onDragEnd    = {
                                scope.launch {
                                    if (netDelta < -10f) sheetState.expand()
                                    else if (netDelta > 10f) sheetState.partialExpand()
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            netDelta += dragAmount
                        }
                    }
            ) {
                VideoLayer(
                    uri           = uri,
                    isVideo       = isVideo,
                    isFrontCamera = isFrontCamera,
                    sheetProgress = sheetProgress,
                    onBack        = onBack
                )
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
        // 9:16 video — scales down from top-center, corners animate as sheet rises
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .graphicsLayer {
                    val s = lerp(1f, 0.60f, sheetProgress)
                    scaleX          = s
                    scaleY          = s
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    val cornerPx    = lerp(0f, 14.dp.toPx(), sheetProgress)
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
private fun EditSheetContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
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

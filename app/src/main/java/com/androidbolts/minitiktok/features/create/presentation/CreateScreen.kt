package com.androidbolts.minitiktok.features.create.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun CreateScreen(
    onClose: () -> Unit = {},
    onMediaCaptured: (Uri, Boolean) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    var permissionsGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        permissionsGranted =
            perms[Manifest.permission.CAMERA] == true &&
            perms[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (permissionsGranted) {
            CameraContent(onCaptured = onMediaCaptured)
        } else {
            PermissionDeniedView {
                launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
            }
        }

        // ── Close (✕) button — always on top ─────────────────────────────────
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onClose
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.Close,
                contentDescription = "Close camera",
                tint               = Color.White,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// ── Camera UI ─────────────────────────────────────────────────────────────────

@SuppressLint("MissingPermission")
@Composable
private fun CameraContent(onCaptured: (Uri, Boolean) -> Unit) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope          = rememberCoroutineScope()

    // ── Use cases & provider from ViewModel (survive screen open/close) ─────────
    val cameraVm: CameraViewModel = viewModel()
    val cameraProvider by cameraVm.provider.collectAsStateWithLifecycle()
    val preview      = cameraVm.preview
    val imageCapture = cameraVm.imageCapture
    val videoCapture = cameraVm.videoCapture

    // ── UI state ──────────────────────────────────────────────────────────────
    var lensFacing      by remember { mutableIntStateOf(cameraVm.lastLensFacing) }
    var isRecording     by remember { mutableStateOf(false) }
    var capturedUri     by remember { mutableStateOf<Uri?>(null) }
    var activeRecording        by remember { mutableStateOf<Recording?>(null) }
    var capturedWithFrontCamera by remember { mutableStateOf(false) }
    val flashAlpha             = remember { Animatable(0f) }
    val isVideo                = capturedUri?.lastPathSegment?.endsWith(".mp4", ignoreCase = true) == true
    // True immediately on re-open (session still warm), false on first open (cold start)
    var isStreaming     by remember { mutableStateOf(cameraVm.boundLensFacing != null) }
    // Fade-out is fast (50ms) so it wins the race against the camera hardware cutoff.
    // Fade-in is smooth (250ms) for a polished appearance.
    val previewAlpha    by animateFloatAsState(
        targetValue   = if (isStreaming) 1f else 0f,
        animationSpec = if (isStreaming) tween(250, easing = FastOutSlowInEasing) else tween(50),
        label         = "preview_alpha"
    )

    // ── PreviewView — surface provider wired immediately in remember ───────────
    // Wiring happens during composition (not in a LaunchedEffect) so the surface
    // is ready before the first frame, shaving one frame off the black-screen wait.
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE // TextureView
            scaleType          = PreviewView.ScaleType.FILL_CENTER
        }.also { pv -> preview.surfaceProvider = pv.surfaceProvider }
    }

    // Persist lens choice and immediately fade-out to hide the camera-switch black gap
    LaunchedEffect(lensFacing) {
        isStreaming = false
        cameraVm.lastLensFacing = lensFacing
    }

    // Observe stream state — fades preview in once real frames are flowing (hides black screen)
    DisposableEffect(previewView, lifecycleOwner) {
        val observer = Observer<PreviewView.StreamState> { state ->
            isStreaming = (state == PreviewView.StreamState.STREAMING)
        }
        previewView.previewStreamState.observe(lifecycleOwner, observer)
        onDispose { previewView.previewStreamState.removeObserver(observer) }
    }

    // ── Bind camera — skip if already bound with the same lens ────────────────
    // Skipping the rebind on re-open means the camera session is reused and the
    // preview shows instantly instead of going black while a new session starts.
    LaunchedEffect(cameraProvider, lensFacing) {
        val provider = cameraProvider ?: return@LaunchedEffect

        if (provider.isBound(preview) && cameraVm.boundLensFacing == lensFacing) {
            // Session already live with the correct lens — just re-wire the surface.
            preview.surfaceProvider = previewView.surfaceProvider
            return@LaunchedEffect
        }

        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        provider.unbindAll()
        cameraVm.boundLensFacing = null
        try {
            provider.bindToLifecycle(
                lifecycleOwner, selector, preview, imageCapture, videoCapture
            )
            cameraVm.boundLensFacing = lensFacing
        } catch (_: Exception) {
            // Fallback: bind preview + video only (some devices reject ImageCapture
            // at HIGHEST equivalent resolution on the back camera)
            try {
                provider.bindToLifecycle(lifecycleOwner, selector, preview, videoCapture)
                cameraVm.boundLensFacing = lensFacing
            } catch (_: Exception) {}
        }
    }

    // Only stop any active recording on leave — camera session stays warm so
    // the next open is instant (ViewModel unbinds when Activity is destroyed).
    DisposableEffect(Unit) {
        onDispose { activeRecording?.stop() }
    }

    // ── Layout ────────────────────────────────────────────────────────────────
    val flash = flashAlpha.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // ── Camera viewfinder (78 % of screen height) ────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.78f)
        ) {
            // CameraX 1.4+ with PreviewView.COMPATIBLE (TextureView) automatically
            // mirrors the front camera via TransformationInfo.isMirroring(). Adding
            // our own scaleX would double-mirror it → unmirrored. So we apply no
            // manual flip. graphicsLayer is kept only for the alpha fade-in on cold open.
            AndroidView(
                factory  = { previewView },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = previewAlpha)
            )

            // White flash on photo shutter
            if (flash > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = flash))
                )
            }

            // Captured media preview — shown inside the same 9:16 frame.
            // Mirror is applied for front-camera captures to match the viewfinder look.
            if (capturedUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    if (isVideo) {
                        val newUrl = capturedUri!!
                        VideoPreview(
                            uri           = newUrl,
                            isFrontCamera = capturedWithFrontCamera,
                            modifier      = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model              = capturedUri,
                            contentDescription = "Preview",
                            modifier           = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = if (capturedWithFrontCamera) -1f else 1f
                                },
                            contentScale       = ContentScale.Fit
                        )
                    }
                }
            }
        }

        // ── Controls area (22 % of screen height) ────────────────────────────
        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .weight(0.22f)
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Context-sensitive hint
                Text(
                    text = when {
                        isRecording         -> "Recording…"
                        capturedUri != null -> ""
                        else                -> "Hold to record  ·  Tap for photo"
                    },
                    color    = Color.White.copy(alpha = 0.82f),
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left — gallery placeholder
                    Spacer(Modifier.size(56.dp))

                    // Center — animated capture button
                    CaptureButton(
                        isRecording = isRecording,

                        // ── Photo capture ──────────────────────────────────────
                        onTap = {
                            val file = File(
                                context.cacheDir,
                                "photo_${System.currentTimeMillis()}.jpg"
                            )
                            imageCapture.takePicture(
                                ImageCapture.OutputFileOptions.Builder(file).build(),
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(out: ImageCapture.OutputFileResults) {
                                        scope.launch {
                                            flashAlpha.animateTo(0.9f, tween(55))
                                            flashAlpha.animateTo(0f,   tween(300))
                                        }
                                        capturedWithFrontCamera = (lensFacing == CameraSelector.LENS_FACING_FRONT)
                                        capturedUri = out.savedUri ?: Uri.fromFile(file)
                                    }
                                    override fun onError(e: ImageCaptureException) {}
                                }
                            )
                        },

                        // ── Video start ────────────────────────────────────────
                        onRecordStart = {
                            isRecording = true
                            capturedWithFrontCamera = (lensFacing == CameraSelector.LENS_FACING_FRONT)
                            val file    = File(
                                context.cacheDir,
                                "video_${System.currentTimeMillis()}.mp4"
                            )
                            activeRecording = videoCapture.output
                                .prepareRecording(
                                    context,
                                    FileOutputOptions.Builder(file).build()
                                )
                                .withAudioEnabled()
                                .start(ContextCompat.getMainExecutor(context)) { event ->
                                    if (event is VideoRecordEvent.Finalize) {
                                        isRecording = false
                                        if (!event.hasError()) {
                                            capturedUri = event.outputResults.outputUri
                                        }
                                        activeRecording = null
                                    }
                                }
                        },

                        // ── Video stop ─────────────────────────────────────────
                        onRecordEnd = { activeRecording?.stop() }
                    )

                    // Right slot — spring cross-fade between flip and tick
                    AnimatedContent(
                        targetState    = capturedUri != null,
                        transitionSpec = {
                            (scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                            (scaleOut(targetScale = 0.55f) + fadeOut(tween(150)))
                        },
                        label = "right_slot"
                    ) { hasCapture ->
                        if (hasCapture) {
                            // ── Tick: confirm and open EditScreen ──────────────
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00C853))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication        = null
                                    ) {
                                        onCaptured(capturedUri!!, capturedWithFrontCamera)
                                        // capturedUri stays non-null during the exit animation so the
                                        // media preview remains visible. CreateScreen recomposes fresh
                                        // (capturedUri = null) when it re-enters on return from Edit.
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.Check,
                                    contentDescription = "Use",
                                    tint               = Color.White,
                                    modifier           = Modifier.size(28.dp)
                                )
                            }
                        } else {
                            // ── Flip camera ────────────────────────────────────
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication        = null
                                    ) {
                                        lensFacing =
                                            if (lensFacing == CameraSelector.LENS_FACING_BACK)
                                                CameraSelector.LENS_FACING_FRONT
                                            else
                                                CameraSelector.LENS_FACING_BACK
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.Refresh,
                                    contentDescription = "Flip camera",
                                    tint               = Color.White,
                                    modifier           = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Permission denied fallback ────────────────────────────────────────────────

@Composable
private fun PermissionDeniedView(onRequest: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(horizontal = 40.dp)
        ) {
            Text("Camera access needed", color = Color.White, fontSize = 20.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text      = "Allow camera and microphone access to record videos and capture photos.",
                color     = Color.White.copy(alpha = 0.60f),
                fontSize  = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Button(onClick = onRequest) { Text("Grant Permission") }
        }
    }
}

package com.androidbolts.minitiktok.features.create.presentation

import android.app.Application
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Survives configuration changes and CreateScreen open/close cycles.
 *
 * Holding use cases here means the camera session is created once and reused —
 * subsequent CreateScreen opens just re-wire the PreviewView surface, giving
 * near-instant camera display without a cold-start black-screen delay.
 */
class CameraViewModel(app: Application) : AndroidViewModel(app) {

    private val _provider = MutableStateFlow<ProcessCameraProvider?>(null)
    val provider: StateFlow<ProcessCameraProvider?> = _provider

    // ── Use cases — one instance for the lifetime of the ViewModel ────────────
    val preview = Preview.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .build()

    val imageCapture: ImageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .build()

    val videoCapture: VideoCapture<Recorder> = VideoCapture.withOutput(
        Recorder.Builder()
            // FHD→HD→SD fallback avoids the silent binding failure that
            // HIGHEST causes on many back cameras when combined with ImageCapture.
            .setQualitySelector(
                QualitySelector.fromOrderedList(
                    listOf(Quality.FHD, Quality.HD, Quality.SD)
                )
            )
            .build()
    )

    /** The lens currently bound to the camera session, or null if unbound. */
    var boundLensFacing: Int? = null

    /** Remembered across CreateScreen open/close so the user's choice is restored. */
    var lastLensFacing: Int = CameraSelector.LENS_FACING_BACK

    init {
        viewModelScope.launch {
            _provider.value = ProcessCameraProvider.awaitInstance(app)
        }
    }

    override fun onCleared() {
        _provider.value?.unbindAll()
        boundLensFacing = null
        super.onCleared()
    }
}

package com.example.carmirror

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import android.view.Surface

/** Glue between the phone-side capture and the car-side Surface. */
object MirrorState {
    var phoneW = 0
    var phoneH = 0
    var touch: TouchService? = null

    private var projection: MediaProjection? = null
    private var surface: Surface? = null
    private var carW = 0
    private var carH = 0
    private var carDpi = 0
    private var display: VirtualDisplay? = null

    @Synchronized fun setProjection(p: MediaProjection) {
        projection = p
        // Required on Android 14+: register a callback before creating a display.
        p.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { release() }
        }, Handler(Looper.getMainLooper()))
        start()
    }

    @Synchronized fun setSurface(s: Surface, w: Int, h: Int, dpi: Int) {
        surface = s; carW = w; carH = h; carDpi = dpi
        start()
    }

    @Synchronized fun clearSurface() {
        display?.release(); display = null
        surface = null
    }

    @Synchronized private fun start() {
        val p = projection ?: return
        val s = surface ?: return
        display?.release()
        // Mirrors the phone's screen straight into the car's Surface.
        display = p.createVirtualDisplay(
            "CarMirror", carW, carH, carDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, s, null, null
        )
    }

    @Synchronized fun release() {
        display?.release(); display = null
        projection?.stop(); projection = null
    }

    /** Car-surface tap -> phone-screen tap (simple proportional mapping). */
    fun tap(x: Float, y: Float) {
        if (carW == 0 || carH == 0) return
        touch?.tap(x * phoneW / carW, y * phoneH / carH)
    }
}

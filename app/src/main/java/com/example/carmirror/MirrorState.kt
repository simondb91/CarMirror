package com.example.carmirror

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import android.view.Surface

/** Glue between the phone-side capture and the car-side Surface. */
object MirrorState {
    // Kept so older code still compiles; the phone size is now read live.
    var phoneW = 0
    var phoneH = 0

    var touch: TouchService? = null
    var onChange: (() -> Unit)? = null

    private var projection: MediaProjection? = null
    private var surface: Surface? = null
    private var carW = 0
    private var carH = 0
    private var carDpi = 0
    private var display: VirtualDisplay? = null

    val hasProjection: Boolean get() = projection != null

    private fun notifyChange() {
        Handler(Looper.getMainLooper()).post { onChange?.invoke() }
    }

    @Synchronized fun setProjection(p: MediaProjection) {
        projection = p
        // Required on Android 14+: register a callback before creating a display.
        p.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { release() }
        }, Handler(Looper.getMainLooper()))
        start()
        notifyChange()
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
        if (carW <= 0 || carH <= 0) return
        display?.release()
        display = p.createVirtualDisplay(
            "CarMirror", carW, carH, carDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, s, null, null
        )
    }

    @Synchronized fun release() {
        display?.release(); display = null
        val p = projection
        projection = null
        p?.stop()
        notifyChange()
    }

    /**
     * Maps a point on the car surface to the phone screen, assuming the phone
     * picture is scaled to fit the car surface and centered (letterboxed).
     */
    private fun map(x: Float, y: Float): Pair<Float, Float>? {
        val (pw, ph) = touch?.phoneSize() ?: return null
        if (carW <= 0 || carH <= 0 || pw <= 0 || ph <= 0) return null
        val scale = minOf(carW.toFloat() / pw, carH.toFloat() / ph)
        val offX = (carW - pw * scale) / 2f
        val offY = (carH - ph * scale) / 2f
        val px = (x - offX) / scale
        val py = (y - offY) / scale
        if (px < 0 || py < 0 || px > pw || py > ph) return null
        return px to py
    }

    /** A tap or swipe on the car screen, replayed on the phone. */
    fun gesture(x1: Float, y1: Float, x2: Float, y2: Float, durationMs: Long) {
        val a = map(x1, y1) ?: return
        val b = map(x2, y2) ?: return
        touch?.swipe(a.first, a.second, b.first, b.second, durationMs)
    }

    fun tap(x: Float, y: Float) = gesture(x, y, x, y, 50)
}

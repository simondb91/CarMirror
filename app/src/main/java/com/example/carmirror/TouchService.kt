package com.example.carmirror

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.hardware.display.DisplayManager
import android.graphics.Path
import android.util.DisplayMetrics
import android.view.Display
import android.view.accessibility.AccessibilityEvent

class TouchService : AccessibilityService() {
    override fun onServiceConnected() { MirrorState.touch = this }
    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { MirrorState.touch = null; super.onDestroy() }

    /** Real phone screen size right now (follows rotation). */
    fun phoneSize(): Pair<Int, Int> {
        val dm = DisplayMetrics()
        @Suppress("DEPRECATION")
        getSystemService(DisplayManager::class.java)
            .getDisplay(Display.DEFAULT_DISPLAY).getRealMetrics(dm)
        return dm.widthPixels to dm.heightPixels
    }

    fun swipe(x1: Float, y1: Float, x2: Float, y2: Float, durationMs: Long) {
        val path = Path().apply {
            moveTo(x1, y1)
            if (x1 != x2 || y1 != y2) lineTo(x2, y2)
        }
        val g = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs.coerceIn(50, 2000)))
            .build()
        dispatchGesture(g, null, null)
    }

    fun tap(x: Float, y: Float) = swipe(x, y, x, y, 50)
}

package com.example.carmirror

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

/** Opened by Android Auto on the car screen (while parked). Shows the phone screen. */
class CarScreenActivity : Activity(), SurfaceHolder.Callback {

    private lateinit var hint: TextView
    private var downX = 0f
    private var downY = 0f
    private var downT = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val match = ViewGroup.LayoutParams.MATCH_PARENT
        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }

        val sv = SurfaceView(this)
        sv.holder.addCallback(this)
        sv.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { downX = e.x; downY = e.y; downT = e.eventTime }
                MotionEvent.ACTION_UP ->
                    MirrorState.gesture(downX, downY, e.x, e.y, e.eventTime - downT)
            }
            true
        }
        root.addView(sv, FrameLayout.LayoutParams(match, match))

        hint = TextView(this).apply {
            text = "Open Car Mirror on your phone and tap\n\"Start screen capture\"."
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
        }
        root.addView(hint, FrameLayout.LayoutParams(match, match))

        val close = TextView(this).apply {
            text = "✕"
            textSize = 28f
            setTextColor(Color.WHITE)
            setPadding(48, 24, 48, 24)
            setOnClickListener { finish() }
        }
        root.addView(close, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.END))

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        MirrorState.onChange = { hint.visibility = if (MirrorState.hasProjection) View.GONE else View.VISIBLE }
        MirrorState.onChange?.invoke()
    }

    override fun onPause() {
        MirrorState.onChange = null
        super.onPause()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        MirrorState.setSurface(holder.surface, width, height, resources.displayMetrics.densityDpi)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) = MirrorState.clearSurface()
}

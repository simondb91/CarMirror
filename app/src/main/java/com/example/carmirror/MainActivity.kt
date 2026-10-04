package com.example.carmirror

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val capture = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { r ->
        val data = r.data ?: return@registerForActivityResult
        startForegroundService(
            Intent(this, CaptureService::class.java)
                .putExtra("code", r.resultCode).putExtra("data", data)
        )
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val dm = resources.displayMetrics
        MirrorState.phoneW = dm.widthPixels
        MirrorState.phoneH = dm.heightPixels

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }
        layout.addView(Button(this).apply {
            text = "1. Start screen capture"
            setOnClickListener {
                val mpm = getSystemService(MediaProjectionManager::class.java)
                capture.launch(mpm.createScreenCaptureIntent())
            }
        })
        layout.addView(Button(this).apply {
            text = "2. Enable touch relay (Accessibility)"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        setContentView(layout)
    }
}

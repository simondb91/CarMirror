package com.example.carmirror

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import androidx.core.content.IntentCompat

class CaptureService : Service() {
    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("mirror", "Mirroring", NotificationManager.IMPORTANCE_LOW))
        val n = Notification.Builder(this, "mirror")
            .setContentTitle("Mirroring to car")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
        startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)

        val code = intent?.getIntExtra("code", 0) ?: return START_NOT_STICKY
        val data = IntentCompat.getParcelableExtra(intent, "data", Intent::class.java)
            ?: return START_NOT_STICKY
        val mpm = getSystemService(MediaProjectionManager::class.java)
        MirrorState.setProjection(mpm.getMediaProjection(code, data))
        return START_NOT_STICKY
    }

    override fun onDestroy() { MirrorState.release(); super.onDestroy() }
}

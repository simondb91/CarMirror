package com.example.carmirror

import android.content.Intent
import androidx.car.app.AppManager
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.validation.HostValidator

class CarMirrorService : CarAppService() {
    // Fine for sideloaded dev use; restrict this if you ever ship it.
    override fun createHostValidator() = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    override fun onCreateSession(): Session = object : Session() {
        override fun onCreateScreen(intent: Intent): Screen = MirrorScreen(carContext)
    }
}

class MirrorScreen(ctx: CarContext) : Screen(ctx) {
    init {
        ctx.getCarService(AppManager::class.java).setSurfaceCallback(object : SurfaceCallback {
            override fun onSurfaceAvailable(c: SurfaceContainer) {
                val s = c.surface ?: return
                MirrorState.setSurface(s, c.width, c.height, c.dpi)
            }
            override fun onSurfaceDestroyed(c: SurfaceContainer) = MirrorState.clearSurface()
            override fun onClick(x: Float, y: Float) = MirrorState.tap(x, y)
        })
    }

    override fun onGetTemplate(): Template = NavigationTemplate.Builder()
        .setActionStrip(
            ActionStrip.Builder()
                .addAction(Action.Builder().setTitle("Exit")
                    .setOnClickListener { carContext.finishCarApp() }.build())
                .build()
        )
        .build()
}

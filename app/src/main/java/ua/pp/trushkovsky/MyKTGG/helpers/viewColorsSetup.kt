package ua.pp.trushkovsky.MyKTGG.helpers

import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import ua.pp.trushkovsky.MyKTGG.R

fun setupView(window: Window?, ctx: Context?, isTransparent: Boolean) {
    val win = window ?: return
    val context = ctx ?: return
    val decorView = win.decorView

    win.navigationBarColor = ContextCompat.getColor(context, R.color.appLightColor)
    win.statusBarColor = ContextCompat.getColor(context, R.color.appLightColor)

    if (isTransparent) {
        win.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )
        win.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
        return
    }

    when {
        Build.VERSION.SDK_INT >= 27 -> {
            decorView.systemUiVisibility = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        Build.VERSION.SDK_INT in 23..26 -> {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            win.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
        }
        else -> {
            win.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
            win.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
        }
    }
}
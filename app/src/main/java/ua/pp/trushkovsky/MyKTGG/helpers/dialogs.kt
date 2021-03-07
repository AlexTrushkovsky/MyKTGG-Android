package ua.pp.trushkovsky.MyKTGG.helpers

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ua.pp.trushkovsky.MyKTGG.R

fun showDialogWith(title: String, body: String, context: Context?, ref: SwipeRefreshLayout?) {
    ref?.isRefreshing = false
    val dialogBuilder = AlertDialog.Builder(context)
    dialogBuilder.setIcon(R.drawable.main_appicon)
    dialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
        dialog.dismiss()
    })
    dialogBuilder.setTitle(title)
    dialogBuilder.setMessage(body)
    val alertDialog = dialogBuilder.create()
    alertDialog.show()
}
fun showDialogWith(body: String, context: Context?, ref: SwipeRefreshLayout?, exception: java.lang.Exception) {
    ref?.isRefreshing = false
    val dialogBuilder = AlertDialog.Builder(context)
    dialogBuilder.setIcon(R.drawable.main_appicon)
    dialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
        dialog.dismiss()
    })
    dialogBuilder.setTitle("Виникла помилка $exception")
    dialogBuilder.setMessage(body)
    val alertDialog = dialogBuilder.create()
    alertDialog.show()
}
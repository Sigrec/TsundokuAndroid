package com.tsundoku.extensions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.widget.Toast

object ContextExt {
    fun Context.getActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }

    fun Context.showToast(message: String?) {
        message?.let {
            val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
            toast.show()
        }
    }

    fun Context.openActionView(uri: Uri) {
        try {
            Intent(Intent.ACTION_VIEW, uri).apply {
                startActivity(this)
            }
        } catch (e: ActivityNotFoundException) {
            Log.e("ANILIST", "No App Found for this Action")
        }
    }
}
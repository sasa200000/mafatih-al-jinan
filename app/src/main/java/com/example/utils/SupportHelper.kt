package com.example.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object SupportHelper {
    const val TELEGRAM_HANDLE = "@sedef3345"
    const val TELEGRAM_USERNAME = "sedef3345"
    const val TELEGRAM_URL = "https://t.me/sedef3345"
    const val TELEGRAM_APP_URI = "tg://resolve?domain=sedef3345"

    fun openSupportTelegram(context: Context) {
        try {
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_APP_URI))
            appIntent.setPackage("org.telegram.messenger")
            appIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(appIntent)
        } catch (_: Exception) {
            try {
                // Fallback to web browser or any handler
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_URL))
                webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(webIntent)
            } catch (_: Exception) {
                copyTelegramId(context)
                Toast.makeText(context, "لینک تلگرام باز نشد؛ آیدی $TELEGRAM_HANDLE کپی شد", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun copyTelegramId(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Telegram Support ID", TELEGRAM_HANDLE)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "آیدی $TELEGRAM_HANDLE کپی شد", Toast.LENGTH_SHORT).show()
    }
}

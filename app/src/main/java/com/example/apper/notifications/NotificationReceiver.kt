package com.example.apper.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.apper.data.Todo

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper = NotificationHelper(context)
        notificationHelper.createNotification()
    }
}